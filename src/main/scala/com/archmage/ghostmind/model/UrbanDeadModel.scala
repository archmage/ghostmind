package com.archmage.ghostmind.model

import java.io.{File, PrintWriter}

import com.archmage.ghostmind.view.StatusBar
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupDocument
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
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
  val skillsUrl = "skills.cgi?"
  val mapUrl = "map.cgi"
  val useragent = "ghostmind (https://github.com/archmage/ghostmind)"

  var sessions: ListBuffer[Option[CharacterSession]] = ListBuffer.fill(3)(None)
  var activeSession: Option[CharacterSession] = None
  val charactersFile = "characters.json"

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
            if(character.isEmpty) None else Some(character.get.decodePassword())
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
        if(character.isEmpty) "{}" else Some(character.get.encodePassword())
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
      try {
        // still need to debug this shit
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
      catch {
        case nfe: NumberFormatException =>
          for(exceptionRow <- contactRows) {
            println(exceptionRow.innerHtml)
          }
          nfe.printStackTrace()
          Contact(args.head.text, args(5).text, args(2).text, -1, -1, id, colour)
      }
    }

    contactsBuffer.clear()
    contactsBuffer ++= contacts
    session.contacts = Some(contacts)

    contacts
  }

  def parseSkills(doc: JsoupDocument): List[String] = {
    List("Skill 1", "Skill 2", "Skill 3")
  }

  def parseMap(doc: JsoupDocument, session: CharacterSession): Option[MapState] = {
    val map = session.browser.get(s"$baseUrl/$mapUrl")
    val gt = (map >> elementList(".gt")).head

    println(gt)
    Some(MapState(60, 50, 88))
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
      saveCharacters()

      StatusBar.status = "loading contacts..."
      session.contacts = Some(parseContactList(contactsDoc.get, session))

      StatusBar.status = "loading skills..."
      val skillsDoc = request(s"$baseUrl/$skillsUrl", session)
      session.skills = Some(parseSkills(skillsDoc.get))

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

//  def loginRequest(username: String, password: String): Boolean = {
//    val session = CharacterSession(username, password)
//    loginExistingSession(session)
//  }
}
