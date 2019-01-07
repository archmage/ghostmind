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

  def parseEventType(eventText: Element): EventType = {
    eventText.text match {
      case Heal.regex(source, target, amount) => Heal(source, target, amount.toInt)
      case Killed.regex(source) => Killed(source)
      case SurvivorKill.regex(source, target, method) => SurvivorKill(source, target, method)
      case Speech.regex(source, message) => Speech(source, message)
      case Broadcast.regex(frequency, message) => Broadcast(frequency, message)
      case Stand.regex(target) => Stand(target)
      case BarricadesDown.regex(source) => BarricadesDown(source)
      case Groan.regex() => Groan()
      case Claw.regex(source, damage) => Claw(source, damage.toInt)
      case Grab.regex() => Grab()
      case Ungrab.regex() => Grab()
      case Bite.regex(source, damage) => Bite(source, damage.toInt)
      case Infect.regex() => Infect()
      case ClawKill.regex(source, target) => ClawKill(source, target)
      case BiteKill.regex(source, target) => BiteKill(source, target)
      case LightsOn.regex(source) => LightsOn(source)
      case LightsOff.regex(source) => LightsOff(source)
      case Flare.regex(source) => Flare(source)
      case Refuel.regex(source) => Refuel(source)
      case Extract.regex(source) => Extract(source)
      case Revive.regex(source) => Revive(source)
      case Dumped.regex(source) => Dumped(source)
      case Attacked.regex(source, verb, weapon, damage) => Attacked(source, verb, weapon, damage.toInt)
      case _ => Default()
    }
  }
}

case class Event(timestamp: ZonedDateTime, content: Element, eventType: EventType) {
  def formatContent(): String = {
    val andAgain = """\.\.\.and again\.""".r.unanchored
    val count = andAgain.findAllIn(content.text).length
    val countString = if(count > 0) s" (x${count + 1})" else ""
//    println(content.innerHtml)
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
abstract class Regex(_regex: String) {
  val regex = _regex.r.unanchored
}

case class Default() extends EventType(AssetManager.eventDefault)

object Heal extends Regex("""(.+?) healed (.+?) for ([0-9]+?) HP\.""")
case class Heal(source: String, target: String, amount: Int) extends EventType(AssetManager.eventHeal)

object Killed extends Regex("""You were killed by (.+?)\.""")
case class Killed(source: String) extends EventType(AssetManager.eventDeath)

object SurvivorKill extends Regex("""(.+?) killed (.+?) with (.+?)\.""")
case class SurvivorKill(source: String, target: String, method: String) extends EventType(AssetManager.eventDeath)

object Speech extends Regex("""(.+?) said "(.+?)"""")
case class Speech(source: String, message: String) extends EventType(AssetManager.eventSpeech)

object Broadcast extends Regex("""([0-9]+?\.[0-9]+?) MHz: "(.+?)"""")
case class Broadcast(frequency: String, message: String) extends EventType(AssetManager.eventRadio)

object Stand extends Regex("""(.+?) stood up\.""")
case class Stand(source: String) extends EventType(AssetManager.eventStand)

object BarricadesDown extends Regex("""(.+?) brought down the last of the barricades\.""")
case class BarricadesDown(source: String) extends EventType(AssetManager.eventAlarm)

object Groan extends Regex(""" groaning """)
case class Groan() extends EventType(AssetManager.eventMegaphone)

object Claw extends Regex("""(.+?) clawed at you for ([0-9]+?) damage\.""")
case class Claw(source: String, damage: Int) extends EventType(AssetManager.eventClaw)

object Grab extends Regex("""The zombie grabbed hold of you!""")
case class Grab() extends EventType(AssetManager.eventClaw)

object Ungrab extends Regex("""The zombie lost its grip\.""")
case class Ungrab() extends EventType(AssetManager.eventClaw)

object Bite extends Regex("""(.+?) bit into you for ([0-9]+?) damage\.""")
case class Bite(source: String, damage: Int) extends EventType(AssetManager.eventBite)

object Infect extends Regex("""The zombie's bite was infected!""")
case class Infect() extends EventType(AssetManager.eventInfect)

object ClawKill extends Regex("""(.+?) killed (.+?)\.""")
case class ClawKill(source: String, target: String) extends EventType(AssetManager.eventDeath)

object BiteKill extends Regex("""(.+?) bit (.+?) to death\.""")
case class BiteKill(source: String, target: String) extends EventType(AssetManager.eventDeath)

object LightsOn extends Regex(""" lights came on inside (.+?)\.""")
case class LightsOn(source: String) extends EventType(AssetManager.eventLightsOn)

object LightsOff extends Regex(""" lights went out in (.+?)\.""")
case class LightsOff(source: String) extends EventType(AssetManager.eventLightsOff)

object Flare extends Regex(""" flare was fired (.+?)\.""")
case class Flare(source: String) extends EventType(AssetManager.eventFlare)

object Refuel extends Regex("""(.+?) refuelled the generator\.""")
case class Refuel(source: String) extends EventType(AssetManager.eventRefuel)

object Extract extends Regex("""(.+?) extracted a DNA sample from you\.""")
case class Extract(source: String) extends EventType(AssetManager.eventExtract)

object Revive extends Regex("""(.+?) revivified .+?\.""")
case class Revive(source: String) extends EventType(AssetManager.eventRevive)

object Dumped extends Regex("""(.+?) dumped your body out onto the street\.""")
case class Dumped(source: String) extends EventType(AssetManager.eventDumped)

object Attacked extends Regex("""(.+?) (.+?) you with a (.+?) for ([0-9]+?) damage\.""")
case class Attacked(source: String, verb: String, weapon: String, damage: Int) extends EventType(AssetManager.eventAttacked)