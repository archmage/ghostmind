package com.archmage.ghostmind.model

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import scalafx.beans.property.ObjectProperty

case class CharacterSession(username: String, password: String) {

  var browser: JsoupBrowser = new JsoupBrowser(UrbanDeadModel.useragent)
  var state: ObjectProperty[SessionState] = ObjectProperty(Offline())

  var hits: Int = 0

  var contacts: Option[List[Contact]] = None
  var skills: Option[List[String]] = None

  var mapState: Option[MapState] = None

  def encodePassword(): PersistentSession = {
    PersistentSession(username, password.getBytes)
  }
}

sealed trait SessionState
case class Offline() extends SessionState
case class Connecting() extends SessionState
case class Online() extends SessionState

case class PersistentSession(username: String, password: Array[Byte]) {
  def decodePassword(): CharacterSession = {
    CharacterSession(username, new String(password))
  }
}