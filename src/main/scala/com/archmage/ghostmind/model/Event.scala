package com.archmage.ghostmind.model

import java.time.{LocalDateTime, ZoneId, ZonedDateTime}

import com.archmage.ghostmind.view.assets.AssetManager
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Element
import scalafx.scene.image.Image

object Event {
  val dateTimeFormatter = Constants.dateTimeFormatter
  var browser: JsoupBrowser = new JsoupBrowser(UrbanDeadModel.useragent)

  val days = ".*?([0-9]+) day.*".r
  val yesterday = ".*?yesterday.*".r
  val hours = ".*?([0-9]+) hour.*".r
  val minutes = ".*?([0-9]+) minute.*".r
  val seconds = ".*?([0-9]+) second.*".r

  def parseTimeText(timeText: String): ZonedDateTime = {
    var time = LocalDateTime.now()
    timeText match {
      case days(number) => time = time.minusDays(number.toInt)
      case yesterday() => time = time.minusDays(1)
      case hours(number) => time = time.minusHours(number.toInt)
      case seconds(number) => time = time.minusSeconds(number.toInt)
      case _ => ()
    }
    // second match for minutes
    timeText match {
      case minutes(number) => time = time.minusMinutes(number.toInt)
      case _ => ()
    }
    time.atZone(ZoneId.systemDefault())
  }

  val healingRegex = """(.+?) healed (.+?) for ([0-9]+?) HP""".r.unanchored
  val deathRegex = """(.+?) killed (.+?) with (.+?)\.""".r.unanchored
  val speakerRegex = """(.+?) said "(.+?)"""".r.unanchored
  val standRegex = """(.+?) stood up\.""".r.unanchored

  def parseEventType(eventText: Element): EventType = {
    eventText.text match {
      case healingRegex(healer, recipient, amount) => Healing(healer, recipient, amount.toInt)
      case deathRegex(killer, victim, method) => Death(killer, victim, method)
      case speakerRegex(speaker, text) => Speech(speaker, text)
      case standRegex(target) => Stand(target)
      case _ => Default()
    }
  }
}

case class Event(timestamp: ZonedDateTime, content: Element, eventType: EventType) {
  def formatContent(): String = {
    val andAgain = """\.\.\.and again\.""".r.unanchored
    val count = andAgain.findAllIn(content.text).length
    val countString = if(count > 0) s" (x${count + 1})" else ""
    content.text.replaceAll(" \\(.+?\\)", "").replaceAll(" \\.\\.\\.and again\\.", "") + countString
  }

  def formatOutput(): String = {
    s"[${Constants.humanReadableFormatter.format(timestamp)}] ${formatContent()}"
  }

  def encode(): PersistentEvent = {
    PersistentEvent(timestamp.format(Event.dateTimeFormatter), content.innerHtml)
  }
}

case class PersistentEvent(timestamp: String, text: String) {
  def decode(): Event = {
    val element = Event.browser.parseString(text).body
    Event(
      LocalDateTime.parse(timestamp, Event.dateTimeFormatter).atZone(ZoneId.systemDefault()),
      element,
      Event.parseEventType(element))
  }
}

sealed abstract class EventType(val image: Image)
case class Default() extends EventType(AssetManager.eventDefault)
case class Healing(healer: String, recipient: String, amount: Int) extends EventType(AssetManager.eventHeal)
case class Death(killer: String, victim: String, method: String) extends EventType(AssetManager.eventDeath)
case class Speech(speaker: String, text: String) extends EventType(AssetManager.eventSpeech)
case class Stand(target: String) extends EventType(AssetManager.eventStand)