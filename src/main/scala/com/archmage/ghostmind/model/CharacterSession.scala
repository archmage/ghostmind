package com.archmage.ghostmind.model

import java.net.UnknownHostException
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
  var attributes: CharacterAttributes = CharacterAttributes()) {

  // stuff that will eventually be moved to attributes
  var events: Option[ListBuffer[Event]] = None
  var contacts: Option[List[Contact]] = None
  var skills: Option[List[String]] = None

  // stuff that should probably persist in some other way
  // actually, won't this be replaced by MapData?
  var environment: Option[String] = None

  // non-persistent variables that are tied to the session's flow
  var browser: JsoupBrowser = new JsoupBrowser(UrbanDeadModel.useragent)
  val state: ObjectProperty[ConnectivityState] = ObjectProperty(Offline)
  var newEvents: Int = 0
  var persist: Boolean = true // if this is false, don't make this session persist!

  val eventsLogFilename: String = s"$username-log.json"

  def requestHit(): Boolean = {
    if(attributes.hits <= 0) false
    else {
      attributes.hits -= 1
      attributes.lastHit = LocalDateTime.now().atZone(ZoneId.systemDefault())
      true
    }
  }

  def getRequest(url: String): Option[Document] = {
    // this is Dangerouse
//    println(url)
    try {
      Some(browser.get(url))
    }
    catch {
      case uhe: UnknownHostException =>
        uhe.printStackTrace()
        None
    }
  }

  def resetBrowser(): Unit = browser = new JsoupBrowser(UrbanDeadModel.useragent)

  def encode(): PersistentSession = PersistentSession(username, password.getBytes, attributes,
    Some(attributes.lastHit.format(CharacterSession.dateTimeFormatter)))
}

// a snapshot of the character's statistics
case class CharacterAttributes(
  var id: Option[Int] = None,
  var hp: Option[Int] = None,
  var ap: Option[Int] = None,
  var level: Option[Int] = None,
  var characterClass: Option[String] = None,
  var xp: Option[Int] = None,
  var description: Option[String] = None,
  var group: Option[String] = None,
  var position: Option[Int] = None,
  var hits: Int = CharacterSession.maxDailyHits,
  var lastHit: ZonedDateTime = LocalDateTime.MIN.atZone(ZoneId.systemDefault())) {

  // eventually, have this be aware of Bodybuilding
  def hpMax(): Int = 50

  def hpString(): String = s"HP: ${hp.getOrElse("???")}/${hpMax()}"
  def hpDouble(): Double = Math.min(hp.getOrElse(hpMax()) / hpMax().doubleValue(), 1)

  /* TODO discern exactly which minute-intervals grant AP
   * Steven Cooper from the RCC mentioned that it works more like a cooldown than a
   * cadence-based income. You use AP, then (apparently) it starts a 30min timer on
   * the server, after which you'll get allocated AP.
   *
   * Investigate this, and confirm its behaviour.
   */
  def apCalculated(): Option[Int] = {
    if(ap.isEmpty) return None
    val now = LocalDateTime.now().atZone(ZoneId.systemDefault())
    val apRecoveredLastHit = LocalDateTime.MIN.until(lastHit, ChronoUnit.MINUTES) / 30
    val apRecoveredNow = LocalDateTime.MIN.until(now, ChronoUnit.MINUTES) / 30
    Some(Math.min(CharacterSession.maxAp, ap.get + (apRecoveredNow - apRecoveredLastHit).intValue()))
  }

  def apString(): String = s"AP: ${apCalculated().getOrElse("???")}/${CharacterSession.maxAp}"
  def apDouble(): Double = Math.max(0, apCalculated().getOrElse(CharacterSession.maxAp)) / CharacterSession.maxAp.doubleValue()

  def hitsString(): String = s"Hits: $hits/${CharacterSession.maxDailyHits}"
  def hitsDouble(): Double = hits / CharacterSession.maxDailyHits.doubleValue()

  def xpString(): String = {s"${xp.getOrElse("???")}xp"}
  def xpDouble(): Double = Math.min(xp.getOrElse(0) / 100.0, 1)

  def xpStringLong(): String = {
    s"${xpString()}${if(xp.isDefined) {
      s" (${xp.get / 75}/${xp.get / 100}/${xp.get / 150})"
    } else ""}"
  }

  def suburbIndex(): Option[Int] = {
    if(position.isEmpty) None
    else Some((position.get / 1000) * 10 + (position.get / 10) % 10)
  }

  def suburbName(): String = {
    if(position.isDefined) Suburb.suburbs(suburbIndex().get).name
    else Suburb.blankSuburb
  }

  def blockName(): String = {
    if(position.isDefined) Block.blocks(position.get).name
    else Block.blankBlock
  }

  def coordinatesString(): String = {
    if(position.isDefined) {
      val block = Block.blocks(position.get)
      s"[${block.x}, ${block.y}]"
    }
    else Block.blankCoordinates
  }
}

/**
  * persistent version of CharacterSession
  */
case class PersistentSession(
  username: String,
  password: Array[Byte],
  attributes: CharacterAttributes,
  lastHit: Option[String]) {

  // TODO use this: https://bintray.com/meetup/maven/json4s-java-time/0.0.9 to replace the lastHit duplication
  def decode(): CharacterSession = {
    val session = CharacterSession(username, new String(password), attributes)

    val lastHitValue = lastHit.getOrElse(LocalDateTime.MIN.format(DateTimeFormatter.ISO_DATE_TIME))
    session.attributes.lastHit = LocalDateTime.parse(lastHitValue, CharacterSession.dateTimeFormatter).atZone(ZoneId.systemDefault())

    // reset hits if rollover has happened
    if(session.attributes.lastHit.until(UrbanDeadModel.getNextRollover, ChronoUnit.HOURS) >= 24) {
      session.attributes.hits = CharacterSession.maxDailyHits
    }

    session
  }
}