package com.archmage.ghostmind.model

import java.time.{LocalDateTime, ZoneId, ZonedDateTime}
import java.time.format.DateTimeFormatter

object Event {
  val dateTimeFormatter = Constants.dateTimeFormatter

  val days = ".*?([0-9]+) day.*".r
  val yesterday = ".*?yesterday.*".r
  val hours = ".*?([0-9]+) hour.*".r
  val minutes = ".*?([0-9]+) minute.*".r
  val seconds = ".*?([0-9]+) second.*".r
  def parseTimeText(timetext: String): ZonedDateTime = {
    var time = LocalDateTime.now()
    timetext match {
      case days(number) => time = time.minusDays(number.toInt)
      case yesterday() => time = time.minusDays(1)
      case hours(number) => time = time.minusHours(number.toInt)
      case seconds(number) => time = time.minusSeconds(number.toInt)
      case _ => ()
    }
    // second match for minutes
    timetext match {
      case minutes(number) => time = time.minusMinutes(number.toInt)
      case _ => ()
    }
    time.atZone(ZoneId.systemDefault())
  }
}

case class Event(timestamp: ZonedDateTime, text: String) {
  def formatOutput(): String = {
    s"(${DateTimeFormatter.ISO_DATE_TIME.format(timestamp)}) $text"
  }

  def encode(): PersistentEvent = {
    PersistentEvent(timestamp.format(Event.dateTimeFormatter), text)
  }
}

case class PersistentEvent(timestamp: String, text: String) {
  def decode(): Event = {
    Event(LocalDateTime.parse(timestamp, Event.dateTimeFormatter).atZone(ZoneId.systemDefault()), text)
  }
}

sealed class EventType
case class Speech(speaker: String, message: String)
case class Healing(healer: String, amount: Int)
case class Lights(on: Boolean, location: String)