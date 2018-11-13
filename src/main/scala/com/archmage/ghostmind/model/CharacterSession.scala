package com.archmage.ghostmind.model

import net.ruippeixotog.scalascraper.browser.JsoupBrowser

class CharacterSession(val username: String, val password: String) {

  val browser: JsoupBrowser = new JsoupBrowser(UrbanDeadModel.useragent)

  var hits: Int = 0

  var contacts: Option[List[Contact]] = None
  var skills: Option[List[String]] = None

  var mapState: Option[MapState] = None
}
