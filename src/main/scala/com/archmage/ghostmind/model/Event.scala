package com.archmage.ghostmind.model

import java.time.{LocalDateTime, ZoneId, ZonedDateTime}

import com.archmage.ghostmind.model.Suburb.dangerMapStyleRegex
import com.archmage.ghostmind.view.assets.AssetManager
import net.ruippeixotog.scalascraper.model.Element
import scalafx.scene.image.Image
import scalafx.scene.paint.{Color, Paint}
import scalafx.scene.text.{Font, FontWeight, Text}

object Event {
  val dateTimeFormatter = Constants.dateTimeFormatter

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
      case Flare.regex(source, coordinates) => Flare(source, coordinates)
      case Refuel.regex(source) => Refuel(source)
      case Extract.regex(source) => Extract(source)
      case Revive.regex(source) => Revive(source)
      case Dumped.regex(source) => Dumped(source)
      case Attacked.regex(source, verb, weapon, damage) => Attacked(source, verb, weapon, damage.toInt)
      case SearchFind.regex(item) => SearchFind(item)
      case SearchDiscard.regex(item) => SearchDiscard(item)
      case SearchEncumbered.regex(item) => SearchEncumbered(item)
      case SearchFail.regex() => SearchFail()
      case _ =>
        println(eventText.text)
        Default()
    }
  }
}

case class Event(timestamp: ZonedDateTime = LocalDateTime.now().atZone(ZoneId.systemDefault()),
                 content: Element,
                 eventType: EventType,
                 position: Option[Int]) {
  def formatContent(): String = {
    val andAgain = """\.\.\.and again\.""".r.unanchored
    val count = andAgain.findAllIn(content.text).length
    val countString = if(count > 0) s" (x${count + 1})" else ""
//    println(content.innerHtml)
    content.text.replaceAll(" \\(.+?\\)", "").replaceAll(" \\.\\.\\.and again\\.", "") + countString
  }

  def formatContentWithTimestamp(): String = {
    s"[${Constants.humanReadableFormatter.format(timestamp)}] ${formatContent()}"
  }

  def textElements(): List[Text] = {
    val list: List[Text] = List(new Text {
      fill = Color.web("#87b0bc")
      text = s"[${Constants.humanReadableFormatter.format(timestamp)}] "
    })

    eventType match {
      case event: TextElements => list ::: event.textElements
      case _ => list :+ new Text {
        fill = Color.White
        text = s"${formatContent()}"
      }
    }
  }

  def encode(): PersistentEvent = {
    PersistentEvent(timestamp.format(Event.dateTimeFormatter), content.innerHtml, position)
  }
}

case class PersistentEvent(timestamp: String, text: String, position: Option[Int]) {
  def decode(): Event = {
    val element = Constants.browser.parseString(text).body
    Event(
      LocalDateTime.parse(timestamp, Event.dateTimeFormatter).atZone(ZoneId.systemDefault()),
      element,
      Event.parseEventType(element),
      position)
  }
}

sealed abstract class EventType(val image: Image)
abstract class Regex(_regex: String) {
  val regex = _regex.r.unanchored
}
trait TextElements {
  def textElements: List[Text]
  final def text(textArg: String, fillArg: Paint = Color.White, fontArg: Font = Font.font(12)): Text = {
    new Text {
      fill = fillArg
      font = fontArg
      text = textArg
    }
  }
}

case class Default() extends EventType(AssetManager.eventDefault)

object Heal extends Regex("""(.+?) healed (.+?) for ([0-9]+?) HP\.""")
case class Heal(source: String, target: String, amount: Int) extends EventType(AssetManager.eventHeal)

object Killed extends Regex("""You were killed by (.+?)\.""")
case class Killed(source: String) extends EventType(AssetManager.eventDeath)
with TextElements {
  override def textElements: List[Text] = List(
    text(s"You were killed by $source.", Color.Tomato, Font.font(null, FontWeight.Bold, 12)))
}

object SurvivorKill extends Regex("""(.+?) killed (.+?) with (.+?)\.""")
case class SurvivorKill(source: String, target: String, method: String) extends EventType(AssetManager.eventDeath)
with TextElements {
  override def textElements: List[Text] = List(
    text(s"$source killed $target with $method.", Color.Tomato, Font.font(null, FontWeight.Bold, 12)))
}

object Speech extends Regex("""(.+?) said "(.+?)"""")
case class Speech(source: String, message: String) extends EventType(AssetManager.eventSpeech)
with TextElements {
  override def textElements: List[Text] = List(
    text(source, Color.web("#d38ba3"), Font.font(null, FontWeight.Bold, 12)),
    text(": "),
    text(s"""$message""", Color.web("#c29fc6"))
  )
}

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
with TextElements {
  override def textElements: List[Text] = List(
    text(s"$source clawed $target to death.", Color.Tomato, Font.font(null, FontWeight.Bold, 12)))
}

object BiteKill extends Regex("""(.+?) bit (.+?) to death\.""")
case class BiteKill(source: String, target: String) extends EventType(AssetManager.eventDeath)
with TextElements {
  override def textElements: List[Text] = List(
    text(s"$source bit $target to death.", Color.Tomato, Font.font(null, FontWeight.Bold, 12)))
}

object LightsOn extends Regex(""" lights came on inside (.+?)\.""")
case class LightsOn(source: String) extends EventType(AssetManager.eventLightsOn)

object LightsOff extends Regex(""" lights went out in (.+?)\.""")
case class LightsOff(source: String) extends EventType(AssetManager.eventLightsOff)

// TODO extract the x/y offset
object Flare extends Regex("""(A flare was fired|.+? fired a flare) ?((?:.+?)?(?: and (?:.+?)?)?)?\.""") {
  val coordinatesRegex = """([0-9]+) blocks? to the (north|south|east|west)(?: and ([0-9]+) blocks? to the (north|south))?""".r.unanchored
}
case class Flare(source: String, coordinates: String) extends EventType(AssetManager.eventFlare) {
  private var _xOffset: Int = 0
  private var _yOffset: Int = 0

  val data = Flare.coordinatesRegex.findAllIn(coordinates)

  for(i <- 2 to Math.min(4, data.groupCount) by 2) {
    val cardinal = data.group(i)
    if(cardinal == "east" || cardinal == "west") {
      _xOffset = if(cardinal == "east") data.group(i - 1).toInt else -data.group(i - 1).toInt
    }
    else if(cardinal == "north" || cardinal == "south") {
      _yOffset = if(cardinal == "south") data.group(i - 1).toInt else -data.group(i - 1).toInt
    }
  }

  def xOffset: Int = _xOffset
  def yOffset: Int = _yOffset
}

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

object SearchFind extends Regex(""".*?(?:Searching|searched) .+? a (.+?)[\.,]""")
case class SearchFind(item: String) extends EventType(AssetManager.eventSearchFind)
with TextElements {
  override def textElements: List[Text] = List(
    text(s"${UrbanDeadModel.activeSession.get.username} searched and found a "),
    text(s"$item.", Color.web("#83dbb0"), Font.font(null, FontWeight.Bold, 12))
  )
}

object SearchDiscard extends Regex(""".+?(?:find|found) a (.+?), and discard(?:ed)? it as useless\.""")
case class SearchDiscard(item: String) extends EventType(AssetManager.eventSearchDiscard)
with TextElements {
  override def textElements: List[Text] = List(
    text(s"${UrbanDeadModel.activeSession.get.username} found a "),
    text(s"$item", Color.web("#d14f65"), Font.font(null, FontWeight.Bold, 12)),
    text(s", and discarded it as useless.")
  )
}

object SearchEncumbered extends Regex(""".+?(?:find|found) a (.+?), but (?:was|are) carrying too much to pick it up\.""")
case class SearchEncumbered(item: String) extends EventType(AssetManager.eventSearchEncumbered)
  with TextElements {
  override def textElements: List[Text] = List(
    text(s"${UrbanDeadModel.activeSession.get.username} found a "),
    text(s"$item", Color.web("#d14f65"), Font.font(null, FontWeight.Bold, 12)),
    text(s", but was carrying too much to pick it up.")
  )
}

object SearchFail extends Regex(""".+? (?:search|searched) .+? nothing\.""")
case class SearchFail() extends EventType(AssetManager.eventSearchFail)
with TextElements {
  override def textElements: List[Text] = List(
    text(s"${UrbanDeadModel.activeSession.get.username} searched and found nothing.", Color.web("#a4a9b2"))
  )
}

