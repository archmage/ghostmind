package com.archmage.ghostmind.model

import java.io.{File, FileOutputStream, PrintWriter}
import java.time._
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.TimeZone

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

  val baseUrl = "http://urbandead.com"
  val contactsUrl = "contacts.cgi?username=%1$s&password=%2$s"
  val skillsUrl = "skills.cgi"
  val mapUrl = "map.cgi"
  val profileUrl = "profile.cgi?id="
  val useragent = "ghostmind (https://github.com/archmage/ghostmind)"

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

  def loadCharacters(completion: () => Unit): Unit = {
    new Thread(() => {
      val file = new File(charactersFile)
      if(file.exists()) {
        val stream = Source.fromFile(file)
        val string = stream.getLines.mkString
        if(!string.isEmpty) {
          val parsed = parse(string)
          val characters: List[JValue] = (parsed \\ "characters").children
          val extracted = characters.map { character =>
            try {
              Some(character.extract[PersistentSession])
            }
            catch {
              case _: MappingException => None
            }
          }
          val mapped = extracted.map { character =>
            if(character.isEmpty) None
            else {
              val session = character.get.decode()
              // logic to reset hits
              if(session.lastHit.until(getNextRollover(), ChronoUnit.HOURS) >= 24) {
                session.hits = CharacterSession.maxDailyHits
              }
              Some(session)
            }
          }.to[ListBuffer]
          sessions = mapped
        }
        stream.close()
      }
      Platform.runLater(() => {
        completion()
      })
    }).start()
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

    parseEvents(map.get, session)

    val gtElements = map.get >> elementList(".gt")
    val statusBlock = gtElements.head
    val locationBlock = gtElements(1)

    parseStatusBlock(statusBlock, session)
    parseLocationBlock(locationBlock, session)
  }

  def parseEvents(doc: JsoupDocument, session: CharacterSession): Unit = {
    val eventsElement = doc >?> element("ul")
    if(eventsElement.isDefined) {
      val events = eventsElement >> elementList("li")
//      val eventsText = events.get.map { _.innerHtml }
      val eventsText = events.get.map { _.text }
      val characterDirectoryFile = new File(characterDirectory)
      if(!characterDirectoryFile.exists()) characterDirectoryFile.mkdir()
      val pw = new PrintWriter(new FileOutputStream(
        new File(s"$characterDirectory/${session.username}-log.md"), true))
      for(line <- eventsText) {
        if(session.events.isEmpty) session.events = Some(ListBuffer())
        val event = Event(Event.parseTimeText(line), line)
        session.events.get += event
        pw.append(event.formatOutput() + "\n")
      }
      pw.close()
    }
  }

  def parseStatusBlock(block: Element, session: CharacterSession): Option[CharacterAttributes] = {
    val boldElements = block >> elementList("b")
    // grab the last 3, since sometimes you're dead
    // this fails if you have 0AP
    val numbers = boldElements.slice(boldElements.size - 3, boldElements.size).map { _.text.toInt }

    // grab the id too?
    val idLink = (block >> element("a")).attr("href")
    val id = idLink.substring(profileUrl.length).toInt

    val profileDoc = request(s"$baseUrl/$profileUrl$id", session)

    if(profileDoc.isEmpty) {
      // throw some more stuff, who even cares lmao
      return None
    }

    val profile = parseProfile(profileDoc.get, session)

    val attributes = Some(CharacterAttributes(id, numbers.head, numbers(2), profile.level, profile.characterClass,
      numbers(1), profile.description, profile.group))
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

  def parseLocationBlock(block: Element, session: CharacterSession): Unit = {
    // same
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
