package com.archmage.ghostmind.model

import java.io.{File, PrintWriter}

import com.archmage.ghostmind.view.StatusBar
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupDocument
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import scalafx.collections.ObservableBuffer
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.write

import scala.io.Source

object UrbanDeadModel {
  implicit val formats = DefaultFormats

  val baseUrl = "http://urbandead.com"
  val contactsUrl = "contacts.cgi?username=%1$s&password=%2$s"
  val skillsUrl = "skills.cgi?"
  val mapUrl = "map.cgi"
  val useragent = "ghostmind (https://github.com/archmage/ghostmind)"

  var sessions = Set[CharacterSession]()
  var activeSession: Option[CharacterSession] = None
  val charactersFile = "characters.json"

  val contactsBuffer: ObservableBuffer[Contact] = new ObservableBuffer[Contact]

  def request(string: String, broser: JsoupBrowser = activeSession.get.browser): Option[JsoupDocument] = {
    if(activeSession.isEmpty) return None

    val response = activeSession.get.browser.get(string)
    println(response.title)
    Some(response)
  }

  def setActiveSession(session: CharacterSession): Unit = {
    if(!sessions.contains(session)) sessions += session
    activeSession = Some(session)
  }

  def loadCharacters(): Unit = {
    val stream = Source.fromFile(charactersFile)
//    sessions = parse(stream.getLines.mkString).extract[Set[CharacterSession]]
    sessions = (parse(stream.getLines.mkString) \\ "characters").extract[Set[CharacterSession]]
    stream.close()

  }

  def saveCharacters(): Unit = {
    val charactersJson = write(sessions)
    val pw = new PrintWriter(new File(charactersFile))
    pw.write(s"""{"characters":$charactersJson}""")
    pw.close()
  }

  def parseContactList(doc: JsoupDocument): List[Contact] = {
    val contactRows = (doc >> elementList("tr")).tail.dropRight(1)

    val contacts = contactRows.map { row =>
      val args = row.children.toList.take(6)
      val name = row.children.head >> element("a")
      val id = name.attr("href").replaceAll("profile\\.cgi\\?id=", "").toInt
      val colour = name.attr("class").replaceAll("con", "").toInt
      Contact(args.head.text, args(5).text, args(2).text, args(3).text.toInt, args(4).text.toInt, id, colour)
    }

    contactsBuffer.clear()
    contactsBuffer ++= contacts

    contacts
  }

  def parseSkills(doc: JsoupDocument): List[String] = {
    List("Skill 1", "Skill 2", "Skill 3")
  }

  def parseMap(doc: JsoupDocument): Option[MapState] = {
    if(activeSession.isEmpty) return None

    val map = activeSession.get.browser.get(s"$baseUrl/$mapUrl")
    val gt = (map >> elementList(".gt")).head

    println(gt)
    Some(MapState(60, 50, 88))
  }

  def loginExistingSession(session: CharacterSession): Boolean = {
    if(activeSession.isEmpty) activeSession = Some(session)
    StatusBar.status = s"""Logging in as "${session.username}"..."""
    try {
      val contactsDoc = request(s"$baseUrl/${
        contactsUrl.format(session.username.replaceAll(" ", "%20"), session.password)}", session.browser)

      // now logged in
      StatusBar.status = "Request successful. Organising sessions..."

      // do some database dumping here?
      setActiveSession(session)
      saveCharacters()

      StatusBar.status = "Loading contacts..."
      session.contacts = Some(parseContactList(contactsDoc.get))

      StatusBar.status = "Loading skills..."
      val skillsDoc = request(s"$baseUrl/$skillsUrl")
      session.skills = Some(parseSkills(skillsDoc.get))

      StatusBar.status = s"""Logged in as "${session.username}"."""
      true
    }
    catch {
      case e: Exception => {
        e.printStackTrace()
        false
      }
    }
  }

  def loginRequest(username: String, password: String): Boolean = {
    if(activeSession.isDefined) return false
    val session = new CharacterSession(username, password)
    try {
      val contactsDoc = request(s"$baseUrl/${contactsUrl.format(username.replaceAll(" ", "%20"), password)}")
      // now logged in

      // do some database dumping here?
      setActiveSession(session)
      saveCharacters()

      session.contacts = Some(parseContactList(contactsDoc.get))
      val skillsDoc = request(s"$baseUrl/$skillsUrl")
      session.skills = Some(parseSkills(skillsDoc.get))
      true
    }
    catch {
      case e: Exception => {
        e.printStackTrace()
        false
      }
    }
  }

}
