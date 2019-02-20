package com.archmage.ghostmind.model

import java.io.{File, FileOutputStream, PrintWriter}
import java.time._

import com.archmage.ghostmind.view.StatusBar
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupDocument
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.Element
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.write
import scalafx.application.Platform
import scalafx.collections.ObservableBuffer

import scala.collection.mutable.ListBuffer
import scala.io.Source

object UrbanDeadModel {
  implicit val formats = DefaultFormats

  val useragent = "ghostmind"
  val baseUrl = "http://urbandead.com"
  val contactsUrl = "contacts.cgi?username=%1$s&password=%2$s"
  val skillsUrl = "skills.cgi"
  val mapUrl = "map.cgi"
  val profileUrl = "profile.cgi?id="

  var sessions: ListBuffer[Option[CharacterSession]] = ListBuffer.fill(3)(None)
  var activeSession: Option[CharacterSession] = None
  val charactersFile = "characters.json"

  val characterDirectory = "characters"

  val contactsBuffer: ObservableBuffer[Contact] = new ObservableBuffer[Contact]

  def request(string: String, session: CharacterSession): Option[JsoupDocument] = {
    val response = session.browser.get(string)
    println(response.title)
    Some(response)
  }

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

  def saveEvents(session: CharacterSession): Unit = {
    val mapped = session.events.getOrElse(ListBuffer()).map { event => event.encode() }
    val eventsJson = write(mapped)

    val characterDirectoryFile = new File(characterDirectory)
    if(!characterDirectoryFile.exists()) characterDirectoryFile.mkdir()
    val pw = new PrintWriter(new FileOutputStream(
      new File(s"$characterDirectory/${session.eventsLogFilename()}")))
    pw.write(s"""{"events":$eventsJson}""")
    pw.close()
  }

  def parseContactList(doc: JsoupDocument, session: CharacterSession): List[Contact] = {
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

  def parseSkills(doc: JsoupDocument): List[String] = {
    List("Skill 1", "Skill 2", "Skill 3")
  }

  // next steps - UI!
  def parseMap(session: CharacterSession): Unit = {
    if(session.hits <= 0) {
      // can't be done, sorry
      return
    }

    session.hits -= 1
    session.lastHit = LocalDateTime.now().atZone(ZoneId.systemDefault())
    val map = request(s"$baseUrl/$mapUrl", session)

    if(map.isEmpty) {
      // throw or something, idfk
      return
    }

    parseNewEvents(map.get, session)

    val gtElements = map.get >> elementList(".gt")
    val mapBlock = (map.get >> elementList("table") >> element(".c")).head
    val statusBlock = gtElements(0)
    val environmentBlock = gtElements(1)

    parseMapBlock(mapBlock, session)
    parseStatusBlock(statusBlock, session)
    parseEnvironmentBlock(environmentBlock, session)
  }

  def parseNewEvents(doc: JsoupDocument, session: CharacterSession): Unit = {
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

      saveEvents(session)
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

    val profileDoc = request(s"$baseUrl/$profileUrl$id", session)

    if(profileDoc.isEmpty) {
      // throw some more stuff, who even cares lmao
      return None
    }

    val profile = parseProfile(profileDoc.get, session)

    val attributes = Some(CharacterAttributes(id, hp, ap, profile.level, profile.characterClass,
      xp, profile.description, profile.group))
    session.attributes = attributes
    attributes
  }

  def parseProfile(doc: JsoupDocument, session: CharacterSession): CharacterAttributes = {
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

  def tryAndSpeak(): Unit = {
    if(activeSession.isEmpty) return
    val speechAttempt = activeSession.get.browser.post(s"$baseUrl/$mapUrl", Map("speech" -> "*makes spooky ghost noises*"))
    println(speechAttempt.body)
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
      val contactsDoc = request(s"$baseUrl/${
        contactsUrl.format(session.username.replaceAll(" ", "%20"), session.password)}", session)

      // now logged in

      // do some database dumping here?
      sessions(index) = Some(session)

      StatusBar.status = "loading contacts..."
      session.contacts = Some(parseContactList(contactsDoc.get, session))

      StatusBar.status = "loading skills..."
      val skillsDoc = request(s"$baseUrl/$skillsUrl", session)
      session.skills = Some(parseSkills(skillsDoc.get))

      StatusBar.status = "loading events log..."
      loadEvents(session)

      StatusBar.status = "checking map.cgi..."
      parseMap(session)

      StatusBar.status = "saving character data..."
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

  def getNextRollover(): ZonedDateTime = {
    // get next midnight in UK
    val midnight = LocalTime.MIDNIGHT
    val today = LocalDate.now(ZoneId.of("Europe/London"))
    val todayMidnight = LocalDateTime.of(today, midnight).atZone(ZoneId.of("Europe/London"))
    val tomorrowMidnight = todayMidnight.plusDays(1)

    tomorrowMidnight
  }
}
