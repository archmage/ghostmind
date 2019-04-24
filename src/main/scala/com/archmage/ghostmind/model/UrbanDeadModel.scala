package com.archmage.ghostmind.model

import java.io.{File, FileOutputStream, PrintWriter}
import java.net.UnknownHostException
import java.time._

import com.archmage.ghostmind.view.StatusBar
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.{Document, Element}
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.write
import org.jsoup.HttpStatusException
import scalafx.collections.ObservableBuffer

import scala.collection.mutable.ListBuffer
import scala.io.Source

/**
  * the big bad model that powers this thing
  * beware: contains plumbing™
  */
object UrbanDeadModel {
  implicit val formats = DefaultFormats

  val useragent = "ghostmind"

  val baseUrl = "http://urbandead.com"
  val contactsUrl = "contacts.cgi?username=%1$s&password=%2$s"
  val skillsUrl = "skills.cgi"
  val mapUrl = "map.cgi"
  val profileUrl = "profile.cgi?id="
  val searchUrl = "map.cgi?search"

  val wikiBaseUrl = "http://wiki.urbandead.com/index.php"
  val wikiSuburbUrl = "Suburb"

  var sessions: ListBuffer[Option[CharacterSession]] = ListBuffer.fill(3)(None)
  var activeSession: Option[CharacterSession] = None

  val charactersFile = "characters.json"
  val characterDirectory = "characters"

  val contactsBuffer: ObservableBuffer[Contact] = new ObservableBuffer[Contact]

  def checkUDServer(): Unit = {
    // check UD server to see if it's reachable
    val serverCheck = try {
      Some(Constants.browser.get(baseUrl))
    }
    catch {
      case uhe: UnknownHostException =>
        uhe.printStackTrace()
        None
      case hse: HttpStatusException =>
        hse.printStackTrace()
        None
    }
    StatusBar.udConnectivity.value = if(serverCheck.isDefined) Online else Offline
  }

  /** load CharacterSession data from file */
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
    val file = new File(s"$characterDirectory/${session.eventsLogFilename}")
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
      new File(s"$characterDirectory/${session.eventsLogFilename}")))
    pw.write(s"""{"events":$eventsJson}""")
    pw.close()
  }

  def saveAll(): Unit = {
    println("saving character states...")
    saveCharacters()
    println("saving event data...")
    sessions.flatten.foreach { session => {
      saveEvents(session)
      println(s"""saved events of "${session.username}"""")
    }}
    println("done saving!")
  }

  def loginExistingSession(session: CharacterSession, index: Int): LoginOutcome = {
    if(session.state.value != Offline) return AlreadyLoggedIn // already in progress / done!

    session.state.value = Connecting
    if(StatusBar.udConnectivity.value == Offline) StatusBar.udConnectivity.value = Connecting

    StatusBar.status = s"""logging in as "${session.username}"..."""

    val contactsResponse = session.getRequest(s"$baseUrl/${
      contactsUrl.format(
        session.username.replaceAll(" ", "%20"),
        session.password.replaceAll(" ", "%20"))}")

    if(contactsResponse.isEmpty) {
      // failed to hit the server, return
      StatusBar.status = s"""failed to reach the server; check your connectivity"""
      session.state.value = Offline
      StatusBar.udConnectivity = Offline
      return ServerInaccessible
    }

    StatusBar.udConnectivity = Online

    val contactsFormOption: Option[Element] = contactsResponse.get >?> element("form")
    if(contactsFormOption.isEmpty) {
      // failed to log in; return
      StatusBar.status = s"""failed to login as "${session.username}"; username or password were incorrect"""
      session.state.value = Offline
      return BadCredentials
    }

    // now logged in
    sessions(index) = Some(session)
    session.state.value = Retrieving

    StatusBar.status = "loading contacts..."
    session.contacts = Some(parseContactList(contactsResponse.get, session))

    StatusBar.status = "loading events log..."
    loadEvents(session)

    StatusBar.status = "checking map.cgi..."
    val mapCgiResponse = pollMapCgi(session)
    if(mapCgiResponse.isEmpty) {
      // failed to hit map.cgi? odd
      StatusBar.status = s"""map.cgi was inaccessible; check your connectivity"""
      session.state.value = Offline
      return ServerInaccessible
    }

    val data = MapData.parseResponse(mapCgiResponse.get)
    processMapCgi(mapCgiResponse.get, session)

    StatusBar.status = "saving character data..."
    saveEvents(session)
    saveCharacters()

    session.state.value = Online
    StatusBar.status = s"""logged in as "${session.username}""""
    Success
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

  def processMapCgi(page: Document, session: CharacterSession): Unit = {

    val mapData = MapData.parseResponse(page)
    session.attributes.lastMapData = Some(mapData)
    println(mapData)

    parseNewEvents(page, session)

    assimilateMapBlock(mapData, session)
    assimilateStatBlock(mapData, session)
    assimilateEnvironmentBlock(mapData, session)
    assimilateInventoryBlock(mapData, session)

    // TODO examine whether this needs to happen literally every single time
    val profileResponse = session.getRequest(s"$baseUrl/$profileUrl${session.attributes.id.get}")
    if(profileResponse.isDefined) parseProfile(profileResponse.get, session)
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
          Event.parseEventType(event),
          session.attributes.position)
        session.events.get += eventInstance
      }
      session.newEvents += events.size
    }
  }

  def assimilateMapBlock(data: MapData, session: CharacterSession): Unit = {
    session.attributes.position = data.mapBlock.position.orElse(session.attributes.position)
  }

  def assimilateStatBlock(data: MapData, session: CharacterSession): Unit = {
    session.attributes.id = session.attributes.id.orElse(Some(data.statBlock.id))
    session.attributes.hp = data.statBlock.hp.orElse(session.attributes.hp)
    session.attributes.xp = data.statBlock.xp.orElse(session.attributes.xp)
    session.attributes.ap = Some(data.statBlock.ap)
  }

  def assimilateEnvironmentBlock(data: MapData, session: CharacterSession): Unit = {
    session.environment = data.environmentBlock.content.orElse(session.environment)
  }

  def assimilateInventoryBlock(data: MapData, session: CharacterSession): Unit = {
    session.attributes.inventory = data.inventoryBlock.inventory.orElse(session.attributes.inventory)
    session.attributes.encumbrance = data.inventoryBlock.encumbrance.orElse(session.attributes.encumbrance)
  }

  def parseProfile(doc: Document, session: CharacterSession): Unit = {
    val rows = doc >> elementList("tr")
    val data = rows.map(_ >> elementList(".slam"))

    // TODO implement this later
    // val description = (doc >> element("td .gp")).text

    session.attributes.level = Some(data(1)(0).text.toInt)
    session.attributes.characterClass = Some(data(0)(0).text)
    session.attributes.group = Some(data(2)(1).text)
  }

  // TODO make this reflect what message actually shows up in-game
  def speakAction(message: String, session: CharacterSession): Option[Document] = {
    if(message.isEmpty || message.length > 250) None // TODO make this not suck
    else {
      val speechAttempt = session.browser.post(s"$baseUrl/$mapUrl", Map("speech" -> message))
      // add an event to the events thing
      session.events.get += new Event(
        content = Constants.browser.parseString(s"""${session.username} said "$message"""").body,
        eventType = Speech(session.username, message),
        position = session.attributes.position)
      saveEvents(session)
      Some(speechAttempt)
    }
  }

  def searchAction(session: CharacterSession): Option[Document] = {
    val searchAttempt = session.browser.post(s"$baseUrl/$searchUrl", Map())
    val message = searchAttempt.body >> element(".gamemessage")
    val eventType = Event.parseEventType(message)
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
        eventType = eventType,
        position = session.attributes.position)
    saveEvents(session)
    Some(searchAttempt)
  }

  def moveAction(session: CharacterSession, x: Int, y: Int): Option[Document] = {
    if(session.attributes.position.isEmpty) return None
    val sessionBlock = Block.blocks(session.attributes.position.get)
    val coordinates =
      (Math.max(0, Math.min(99, sessionBlock.x + Math.signum(x))).toInt,
        Math.max(0, Math.min(99, sessionBlock.y + Math.signum(y))).toInt)
    val moveAttempt = session.browser.post(s"$baseUrl/$mapUrl", Map("v" ->
      s"${coordinates._1}-${coordinates._2}"))

    Some(moveAttempt)
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
