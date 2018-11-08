package com.archmage.ghostmind.model

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import scalafx.collections.ObservableBuffer
import scalaj.http.{Http, HttpResponse}

object UrbanDeadModel {

  val baseUrl = "http://urbandead.com"
  val contactsUrl = "contacts.cgi?username=%1$s&password=%2$s"
  val useragent = "ghostmind (https://github.com/archmage/ghostmind)"
  val browser = JsoupBrowser()

  val contactsBuffer = ObservableBuffer[Contact]()

  def loadContactsList(username: String, password: String): HttpResponse[String] = {
    request(s"$baseUrl/${contactsUrl.format(username.replaceAll(" ", "%20"), password)}")
  }

  def parseContactList(body: String): List[Contact] = {
    val doc = browser.parseString(body)
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

  // core request function that simplifies header assignment and response formatting
  def request(string: String): HttpResponse[String] = {
    val request = Http(string)
    request.header("User-Agent", useragent)
    val response = request.asString
    println(request)
    println(s"response: ${response.code}")
    if(response.isError) response.throwError
    response
  }
}
