package com.archmage.ghostmind.model

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{LocalDateTime, ZoneId, ZonedDateTime}

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import scalafx.beans.property.ObjectProperty

import scala.collection.mutable.ListBuffer

object CharacterSession {
  val maxAp = 50
  val maxDailyHits: Int = 300
  val dateTimeFormatter = Constants.dateTimeFormatter
}

case class CharacterSession(
  username: String,
  password: String,
  var attributes: Option[CharacterAttributes] = None) {

  var browser: JsoupBrowser = new JsoupBrowser(UrbanDeadModel.useragent)
  val state: ObjectProperty[SessionState] = ObjectProperty(Offline())

  var contacts: Option[List[Contact]] = None
  var skills: Option[List[String]] = None

  var events: Option[ListBuffer[Event]] = None

  var hits: Int = CharacterSession.maxDailyHits
  var lastHit: ZonedDateTime = LocalDateTime.MIN.atZone(ZoneId.systemDefault())

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
}

case class CharacterAttributes(
  id: Int,
  hp: Int, // make you optional, friendo
  ap: Int,
  level: Int,
  characterClass: String,
  xp: Int,
  description: String,
  group: String) {
}

sealed trait SessionState
// TODO make these case objects and drop the parenthesess
case class Offline() extends SessionState
case class Connecting() extends SessionState
case class Online() extends SessionState

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
    session
  }
}