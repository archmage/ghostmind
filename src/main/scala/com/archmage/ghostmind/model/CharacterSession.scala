package com.archmage.ghostmind.model

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId, ZonedDateTime}

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import scalafx.beans.property.ObjectProperty

import scala.collection.mutable.ListBuffer

object CharacterSession {
  val maxDailyHits: Int = 300
  val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
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

  def resetBrowser(): Unit = {
    browser = new JsoupBrowser(UrbanDeadModel.useragent)
  }
}

case class CharacterAttributes(
  id: Int,
  hp: Int,
  ap: Int,
  level: Int,
  characterClass: String,
  xp: Int,
  description: String,
  group: String) {

  def hpDouble(): Double = {
    Math.min(hp / 50.0, 1)
  }

  def apDouble(): Double = {
    ap / 50.0
  }
}

sealed trait SessionState
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