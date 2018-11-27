package com.archmage.ghostmind.model

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import scalafx.beans.property.ObjectProperty

import scala.collection.mutable.ListBuffer

case class CharacterSession(
  username: String,
  password: String,
  var attributes: Option[CharacterAttributes] = None) {

  var browser: JsoupBrowser = new JsoupBrowser(UrbanDeadModel.useragent)
  var state: ObjectProperty[SessionState] = ObjectProperty(Offline())

  var hits: Int = 0

  var contacts: Option[List[Contact]] = None
  var skills: Option[List[String]] = None

  var events: Option[ListBuffer[Event]] = None

  def encodePassword(): PersistentSession = {
    PersistentSession(username, password.getBytes, attributes)
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
  group: String
)

sealed trait SessionState
case class Offline() extends SessionState
case class Connecting() extends SessionState
case class Online() extends SessionState

case class PersistentSession(username: String, password: Array[Byte], attributes: Option[CharacterAttributes] = None) {
  def decodePassword(): CharacterSession = {
    CharacterSession(username, new String(password), attributes)
  }
}