package com.archmage.ghostmind.model

import java.rmi.UnknownHostException
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{LocalDateTime, ZoneId, ZonedDateTime}

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document
import scalafx.beans.property.ObjectProperty

import scala.collection.mutable.ListBuffer

object CharacterSession {
  val maxAp = 50
  val maxDailyHits: Int = 300
  val dateTimeFormatter = Constants.dateTimeFormatter
}

/**
  * case class to represent a character session's state
  */
case class CharacterSession(
  username: String,
  password: String,
  var attributes: Option[CharacterAttributes] = None) {

  var position: Option[Int] = None
  var environment: Option[String] = None

  var browser: JsoupBrowser = new JsoupBrowser(UrbanDeadModel.useragent)
  val state: ObjectProperty[ConnectivityState] = ObjectProperty(Offline)

  var contacts: Option[List[Contact]] = None
  var skills: Option[List[String]] = None

  var events: Option[ListBuffer[Event]] = None
  var newEvents: Int = 0

  var hits: Int = CharacterSession.maxDailyHits
  var lastHit: ZonedDateTime = LocalDateTime.MIN.atZone(ZoneId.systemDefault())

  def requestHit(): Boolean = {
    if(hits <= 0) false
    else {
      hits -= 1
      lastHit = LocalDateTime.now().atZone(ZoneId.systemDefault())
      true
    }
  }

  def getRequest(url: String): Option[Document] = {
    println(url)
    try {
      Some(browser.get(url))
    }
    catch {
      case uhe: UnknownHostException =>
        uhe.printStackTrace()
        None
    }
  }

  def hitsDouble(): Double = {
    hits / CharacterSession.maxDailyHits.doubleValue()
  }

  def encode(): PersistentSession = {
    PersistentSession(username, password.getBytes, attributes, hits,
      Some(lastHit.format(CharacterSession.dateTimeFormatter)))
  }

  def eventsLogFilename(): String = s"$username-log.json"

  def resetBrowser(): Unit = {
    browser = new JsoupBrowser(UrbanDeadModel.useragent)
  }

  def hpValue(): Int = {
    // eventually, have this be aware of Bodybuilding
    if(attributes.isDefined) attributes.get.hp else hpMax()
  }

  def hpMax(): Int = {
    // eventually, have this be aware of Bodybuilding

    /*
    attributes.hp becomes optional
    when parsing from map.cgi, if it's not there, defer to existing hp value
    if _it's_ not there, return None
    parse None as ??? for text and 100% (50 or 60 with Bodybuilding) for number things
     */

    50
  }

  def hpDouble(): Double = {
    Math.min(hpValue() / hpMax().doubleValue(), 1)
  }

  def hpString(): String = {
    s"HP: ${if(attributes.isEmpty) "???" else hpValue()}/${hpMax()}"
  }

  def apString(): String = {
    s"AP: ${if(attributes.isEmpty) "???" else apCalculated()}/${CharacterSession.maxAp}"
  }

  def apCalculated(): Int = {
    if(attributes.isEmpty) CharacterSession.maxAp
    else {
      // something in here is wrong, sadly
      val now = LocalDateTime.now().atZone(ZoneId.systemDefault())
      val apRecoveredLastHit = LocalDateTime.MIN.until(lastHit, ChronoUnit.MINUTES) / 30
      val apRecoveredNow = LocalDateTime.MIN.until(now, ChronoUnit.MINUTES) / 30
      Math.min(CharacterSession.maxAp, attributes.get.ap + (apRecoveredNow - apRecoveredLastHit).intValue())
    }
  }

  def apDouble(): Double = {
    Math.max(0, apCalculated()) / CharacterSession.maxAp.doubleValue()
  }

  def hitsString(): String = {
    s"Hits: $hits/${CharacterSession.maxDailyHits}"
  }

  def xpString(): String = {
    s"${attributes.get.xp}xp"
  }

  def xpStringLong(): String = {
    s"${xpString()} (${attributes.get.xp / 75}/${attributes.get.xp / 100}/${attributes.get.xp / 150})"
  }

  def suburbIndex(): Option[Int] = {
    if(position.isEmpty) None
    else Some((position.get / 1000) * 10 + (position.get / 10) % 10)
  }
}

case class CharacterAttributes(
  id: Int,
  hp: Int, // TODO make you optional, friendo
  ap: Int,
  level: Int,
  characterClass: String,
  xp: Int,
  description: String,
  group: String) {
}

/**
  * persistent version of CharacterSession
  */
case class PersistentSession(
  username: String,
  password: Array[Byte],
  attributes: Option[CharacterAttributes],
  hits: Int,
  lastHit: Option[String]) {

  def decode(): CharacterSession = {
    val session = CharacterSession(username, new String(password), attributes)
    session.hits = hits
    val lastHitValue = lastHit.getOrElse(LocalDateTime.MIN.format(DateTimeFormatter.ISO_DATE_TIME))
    session.lastHit = LocalDateTime.parse(lastHitValue, CharacterSession.dateTimeFormatter).atZone(ZoneId.systemDefault())

    // reset hits if rollover has happened
    if(session.lastHit.until(UrbanDeadModel.getNextRollover, ChronoUnit.HOURS) >= 24) {
      session.hits = CharacterSession.maxDailyHits
    }

    session
  }
}