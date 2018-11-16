package com.archmage.ghostmind.model

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import scalafx.beans.property.ObjectProperty

class CharacterSession(val username: String, val password: String) {

  var browser: JsoupBrowser = new JsoupBrowser(UrbanDeadModel.useragent)
  var state: ObjectProperty[SessionState] = ObjectProperty(Offline())

  var hits: Int = 0

  var contacts: Option[List[Contact]] = None
  var skills: Option[List[String]] = None

  var mapState: Option[MapState] = None
}

sealed trait SessionState
case class Offline() extends SessionState
case class Connecting() extends SessionState
case class Online() extends SessionState