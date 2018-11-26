package com.archmage.ghostmind.model

import java.time.{LocalDateTime, ZoneId}

import org.ocpsoft.prettytime.nlp.PrettyTimeParser

object Event {
  def parseTimeText(timetext: String): LocalDateTime = {
    val parser = new PrettyTimeParser
    val date = parser.parse(timetext).get(0)
    date.toInstant.atZone(ZoneId.systemDefault()).toLocalDateTime
  }
}

case class Event(timestamp: LocalDateTime, text: String) {

}
