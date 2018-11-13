package com.archmage.ghostmind.model

import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupDocument
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import scalafx.collections.ObservableBuffer

object UrbanDeadModel {

  val baseUrl = "http://urbandead.com"
  val contactsUrl = "contacts.cgi?username=%1$s&password=%2$s"
  val skillsUrl = "skills.cgi?"
  val mapUrl = "map.cgi"
  val useragent = "ghostmind (https://github.com/archmage/ghostmind)"

  var activeSession: Option[CharacterSession] = None

  val contactsBuffer: ObservableBuffer[Contact] = new ObservableBuffer[Contact]

  def loginRequest(username: String, password: String): Boolean = {
    if(activeSession.isDefined) return false
    val session = new CharacterSession(username, password)
    activeSession = Some(session)
    try {
      val contactsDoc = request(s"$baseUrl/${contactsUrl.format(username.replaceAll(" ", "%20"), password)}")
      // now logged in
      println(session.browser.cookies(s"$baseUrl/${contactsUrl.format(username.replaceAll(" ", "%20"), password)}"))
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
    return List("Skill 1", "Skill 2", "Skill 3")
  }

  def parseMap(doc: JsoupDocument): Option[MapState] = {
    if(activeSession.isEmpty) return None

    val map = activeSession.get.browser.get(s"$baseUrl/$mapUrl")
    val gt = (map >> elementList(".gt")).head

    println(gt)
    Some(MapState(60, 50, 88))
  }

  def request(string: String): Option[JsoupDocument] = {
    if(activeSession.isEmpty) return None

    val response = activeSession.get.browser.get(string)
    println(response.title)
    Some(response)
  }
}
