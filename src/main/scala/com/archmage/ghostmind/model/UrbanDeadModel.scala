package com.archmage.ghostmind.model

import java.io.{File, FileOutputStream, PrintWriter}
import java.time._
import java.time.temporal.ChronoUnit

import com.archmage.ghostmind.view.StatusBar
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.{Document, Element}
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.write
import scalafx.application.Platform
import scalafx.collections.ObservableBuffer

import scala.collection.mutable.ListBuffer
import scala.io.Source

// contains plumbing - be warned
object UrbanDeadModel {
  implicit val formats = DefaultFormats

  val useragent = "ghostmind"
  val baseUrl = "http://urbandead.com"
  val contactsUrl = "contacts.cgi?username=%1$s&password=%2$s"
  val skillsUrl = "skills.cgi"
  val mapUrl = "map.cgi"
  val profileUrl = "profile.cgi?id="
  val searchUrl = "map.cgi?search"

  var sessions: ListBuffer[Option[CharacterSession]] = ListBuffer.fill(3)(None)
  var activeSession: Option[CharacterSession] = None
  val charactersFile = "characters.json"

  val characterDirectory = "characters"

  val contactsBuffer: ObservableBuffer[Contact] = new ObservableBuffer[Contact]

  def loadCharacters(): Option[ListBuffer[Option[CharacterSession]]] = {
    val file = new File(charactersFile)
    if(!file.exists()) return None

    val stream = Source.fromFile(file)
    val string = stream.getLines.mkString
    if(string.isEmpty) return None

    val parsed = parse(string)
    val characters: List[JValue] = (parsed \\ "characters").children
    val extracted = characters.map { character =>
      try { Some(character.extract[PersistentSession]) }
      catch { case _: MappingException => None }
    }
    val decoded = extracted.map { character =>
      if(character.isEmpty) None else Some(character.get.decode())
    }.to[ListBuffer]
    sessions = decoded

    stream.close()

    Some(sessions)
  }

  def saveCharacters(): Unit = {
    new Thread(() => {
      val mapped = sessions.map { character =>
        if(character.isEmpty) "{}" else Some(character.get.encode())
      }
      val charactersJson = write(mapped)
      val pw = new PrintWriter(new File(charactersFile))
      pw.write(s"""{"characters":$charactersJson}""")
      pw.close()
    }).start()
  }

  def loadEvents(session: CharacterSession): Unit = {
    val file = new File(s"$characterDirectory/${session.eventsLogFilename()}")
    if(file.exists()) {
      val stream = Source.fromFile(file)
      val string = stream.getLines.mkString
      if(!string.isEmpty) {
        val parsed = parse(string)
        val events = (parsed \\ "events").extract[ListBuffer[PersistentEvent]].map { _.decode() }
        stream.close()
        session.events = Some(events)
      }
      else {
        stream.close()
      }
    }
  }

  // TODO examine how this loading and saving is done, it has potential for data loss
  def saveEvents(session: CharacterSession): Unit = {
    // can't save if there's nothing there
    if(session.events.isEmpty) return

    val mapped = session.events.get.map { event => event.encode() }
    val eventsJson = write(mapped)

    val characterDirectoryFile = new File(characterDirectory)
    if(!characterDirectoryFile.exists()) characterDirectoryFile.mkdir()
    val pw = new PrintWriter(new FileOutputStream(
      new File(s"$characterDirectory/${session.eventsLogFilename()}")))
    pw.write(s"""{"events":$eventsJson}""")
    pw.close()
  }

  def saveAll(): Unit = {
    saveCharacters()
    sessions.flatten.foreach { session => saveEvents(session) }
  }

  def parseContactList(doc: Document, session: CharacterSession): List[Contact] = {
    val contactRows = (doc >> elementList("tr")).tail.dropRight(1)

    val contacts = contactRows.map { row =>
      val args = row.children.toList.take(6)
      val name = row.children.head >> element("a")
      val id = name.attr("href").replaceAll("profile\\.cgi\\?id=", "").toInt
      val colour = name.attr("class").replaceAll("con", "").toInt

      val pattern = """.*?([0-9]+).*""".r
      val level = args(3).text match {
        case pattern(levelString) => levelString.toInt
        case _ => -1
      }
      val xp = args(4).text match {
        case pattern(xpString) => xpString.toInt
        case _ => -1
      }
      Contact(args.head.text, args(5).text, args(2).text, level, xp, id, colour)
    }

    contactsBuffer.clear()
    contactsBuffer ++= contacts
    session.contacts = Some(contacts)

    contacts
  }

  def pollMapCgi(session: CharacterSession): Option[Document] = {
    if(!session.requestHit()) None
    else session.getRequest(s"$baseUrl/$mapUrl")
  }

  def parseMapCgi(page: Document, session: CharacterSession): Unit = {
    parseNewEvents(page, session)

    val gtElements = page >> elementList(".gt")
    val mapBlock = (page >> elementList("table") >> element(".c")).head
    val statusBlock = gtElements(0)
    val environmentBlock = gtElements(1)

    parseMapBlock(mapBlock, session)
    parseStatusBlock(statusBlock, session)
    parseEnvironmentBlock(environmentBlock, session)
  }

  def parseNewEvents(doc: Document, session: CharacterSession): Unit = {
    session.newEvents = 0
    val eventsElement = doc >?> element("ul")
    if(eventsElement.isDefined) {
      val events = (eventsElement >> elementList("li")).get // if this breaks i'm a moron
      for(event <- events) {
        if(session.events.isEmpty) session.events = Some(ListBuffer())
        val eventInstance = Event(
          Event.parseTimeText(event.text),
          event,
          Event.parseEventType(event))
        session.events.get += eventInstance
      }
      session.newEvents += events.size
    }
  }

  def parseMapBlock(block: Element, session: CharacterSession): Unit = {
    try {
      val centreRow = (block >> elementList("tr"))(2)
      val inputs = centreRow >> elementList("input")
      val hiddenInputs = inputs.filter(element => element.attr("type") == "hidden")
      val coordinates = hiddenInputs.map { input =>
        val xy = input.attr("value").split("-")
        (xy(0).toInt, xy(1).toInt)
      }
      var x = 0
      var y = 0

      if(coordinates.length == 2) {
        x = coordinates(0)._1 + 1
        y = coordinates(0)._2
      }
      else {
        y = coordinates(0)._2
        if(coordinates(0)._1 == 1) x = 0
        else if(coordinates(0)._1 == 98) x = 99
      }

      session.position = Some(x + y * 100)
    }
    catch {
      case _: IndexOutOfBoundsException => ()
    }
  }

  def parseStatusBlock(block: Element, session: CharacterSession): Option[CharacterAttributes] = {
    val boldElements = block >> elementList("b")

    // i don't like defaulting to this without some sort of UI indication that this has happened
    // maybe make attributes.hp optional and show ??? when None
    // probably do this later, it's an edge case
    var hp = 50

    var xp = 0
    var ap = 0
    if(boldElements.length <= 2) {
      ap = boldElements.last.text.toInt
    }
    else {
      val numbers = boldElements.slice(boldElements.size - 3, boldElements.size)
      hp = numbers.head.text.toInt
      xp = numbers(1).text.toInt
      ap = numbers(2).text.toInt
    }

    // grab the id too?
    val idLink = (block >> element("a")).attr("href")
    val id = idLink.substring(profileUrl.length).toInt

    val profileResponse = session.getRequest(s"$baseUrl/$profileUrl$id")

    if(profileResponse.isEmpty) {
      // throw some more stuff, who even cares lmao
      return None
    }

    val profile = parseProfile(profileResponse.get, session)

    val attributes = Some(CharacterAttributes(id, hp, ap, profile.level, profile.characterClass,
      xp, profile.description, profile.group))
    session.attributes = attributes
    attributes
  }

  def parseProfile(doc: Document, session: CharacterSession): CharacterAttributes = {
    val rows = doc >> elementList("tr")
    val data = rows.map { _ >> elementList(".slam") }
    // TODO implement this later
//    val description = (doc >> element("td .gp")).text
    val group = data(2)(1).text
    CharacterAttributes(-1, -1, -1, data(1).head.text.toInt, data.head.head.text, -1, "", group)
  }

  def parseEnvironmentBlock(block: Element, session: CharacterSession): Unit = {
//    println(block.innerHtml)
    session.environment = Some(block.text)
  }

  // TODO make this reflect what message actually shows up in-game
  def speakAction(message: String, session: CharacterSession): Option[Document] = {
    if(message.isEmpty || message.length > 250) None
    else {
      val speechAttempt = session.browser.post(s"$baseUrl/$mapUrl", Map("speech" -> message))
      // add an event to the events thing
      session.events.get += new Event(
        content = Constants.browser.parseString(s"""${session.username} said "$message"""").body,
        eventType = Speech(session.username, message))
      Some(speechAttempt)
    }
  }

  val searchFailRegex = """(.+\.)( \(x([0-9]+?)\))?""".r.unanchored

  def searchAction(session: CharacterSession): Option[Document] = {
    val searchAttempt = session.browser.post(s"$baseUrl/$searchUrl", Map())
    val message = searchAttempt.body >> element(".gamemessage")
    val eventType = Event.parseEventType(message)
    // 3min condense
    // this is busted af
//    if(session.events.nonEmpty) {
//      val maybeFail = session.events.get.head
//      maybeFail.eventType match {
//        case _: SearchFail =>
//          if(maybeFail.timestamp.until(
//            LocalDateTime.now().atZone(ZoneId.systemDefault()),
//            ChronoUnit.MINUTES) < 3) {
//            session.events.get.dropRight(1)
//            val regexResults = searchFailRegex.findAllIn(maybeFail.content.text)
//            val newContent = s"${regexResults.group(1)} (x${
//              if(regexResults.groupCount <= 1) 2 else regexResults.group(3) + 1 })"
//            session.events.get += new Event(
//              content = Constants.browser.parseString(newContent).body,
//              eventType = SearchFail()
//            )
//          }
//        case _ => session.events.get += new Event(
//          content = Constants.browser.parseString(
//            s"""${session.username} searched and found ${
//              eventType match {
//                case find: SearchFind => s"a ${find.item}"
//                case _ => "nothing"
//              }
//            }.""").body,
//          eventType = eventType
//        )
//      }
//    }
//    else {
      session.events.get += new Event(
        content = Constants.browser.parseString(
          s"""${session.username} ${
            eventType match {
              case find: SearchFind => s"searched and found a ${find.item}."
              case discard: SearchDiscard => s"found a ${discard.item}, and discarded it as useless."
              case encumbered: SearchEncumbered => s"found a ${encumbered.item}, but was carrying too much to pick it up."
              case _ => "searched and found nothing."
            }
          }""").body,
        eventType = eventType
      )
//    }
    Some(searchAttempt)
  }

  def tryAndRevive(): Unit = {
    if(activeSession.isEmpty) return
    val reviveAttempt = activeSession.get.browser.post(s"$baseUrl/$mapUrl?use-z", Map(
      "target" -> "target-id-this-is-intentionally-broken"
    ))
    println(reviveAttempt.body)
  }

  def loginExistingSession(session: CharacterSession, index: Int): Boolean = {
    if(session.state.value != Offline()) return true // already in progress / done!

    Platform.runLater(() => {
      session.state.value = Connecting()
    })
    StatusBar.status = s"""logging in as "${session.username}"..."""
    try {
      val contactsResponse = session.getRequest(s"$baseUrl/${
        contactsUrl.format(session.username.replaceAll(" ", "%20"), session.password)}")

      // now logged in

      // do some database dumping here?
      sessions(index) = Some(session)

      StatusBar.status = "loading contacts..."
      session.contacts = Some(parseContactList(contactsResponse.get, session))

      // return to this later
//      StatusBar.status = "loading skills..."
//      val skillsDoc = session.getRequest(s"$baseUrl/$skillsUrl")

      StatusBar.status = "loading events log..."
      loadEvents(session)

      StatusBar.status = "checking map.cgi..."
      val mapCgiResponse = pollMapCgi(session)
      if(mapCgiResponse.isDefined) {
        val data = MapData.parseResponse(mapCgiResponse.get)
        parseMapCgi(mapCgiResponse.get, session)
      }
      
      StatusBar.status = "saving character data..."
      saveEvents(session)
      saveCharacters()

      Platform.runLater(() => {
        session.state.value = Online()
      })
      StatusBar.status = s"""logged in as "${session.username}""""
      true
    }
    catch {
      case e: Exception =>
        e.printStackTrace()
        false
    }
  }

  def getNextRollover: ZonedDateTime = {
    // get next midnight in UK
    val midnight = LocalTime.MIDNIGHT
    val today = LocalDate.now(ZoneId.of("Europe/London"))
    val todayMidnight = LocalDateTime.of(today, midnight).atZone(ZoneId.of("Europe/London"))
    val tomorrowMidnight = todayMidnight.plusDays(1)

    tomorrowMidnight
  }
}
