package com.archmage.ghostmind.model

import com.archmage.ghostmind.model.UrbanDeadModel.{baseUrl, parseProfile, profileUrl}
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.{Document, Element}
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.{element, elementList}

import scala.collection.mutable.ListBuffer

object MapData {
  // TODO fix this later
  def parseStatusBlock(block: Element): Unit = {
    val boldElements = block >> elementList("b")

    // i don't like defaulting to this without some sort of UI indication that this has happened
    // maybe make attributes.hp optional and show ??? when None
    // probably do this later, it's an edge case
    var hp = 50

    var xp = 0
    var ap = 0
    if(boldElements.length <= 2) {
      ap = boldElements.last.text.toInt
    }
    else {
      val numbers = boldElements.slice(boldElements.size - 3, boldElements.size)
      hp = numbers.head.text.toInt
      xp = numbers(1).text.toInt
      ap = numbers(2).text.toInt
    }
    val username = (block >> element("a")).text

    // handle hp/xp fails better
    // return statblock
  }

  def parseResponse(response: Document): MapData = {
    // stuff here eventually

    MapData(response, "", 0, 0, 0, 0, None, None, None, "", ListBuffer(), "", "", 0, ListBuffer(), ListBuffer(),
      ListBuffer(), 0, ListBuffer(), ListBuffer(), "", ListBuffer())
  }
}

case class StatBlock (
  username: String,
  hp: Option[Int],
  ap: Option[Int],
  xp: Option[Int])

// rationalisable form of map.cgi responses
// TODO add gamemessage
case class MapData (
  response: Document,
  username: String,
  hp: Int,
  ap: Int,
  xp: Int,
  location: Int,
  safehouse: Option[Int],
  inside: Option[Boolean],
  barricadeLevel: Option[Int],
  locationFlavour: String,
  occupants: ListBuffer[String], // username, profile ID, contact colour, HP
  radio: Any, // frequency, condition?
  generator: Any, // health, powered/unpowered, isRunningLow
  zombieCount: Int,
  knownZombies: ListBuffer[String], // prolly same data object as occupants, but without HP
  actions: ListBuffer[String], // no idea how to represent this
  inventory: ListBuffer[Any], // need an object for items
  encumberance: Int,
  dropList: ListBuffer[String], // list of items you could drop; unsure on this
  targetList: ListBuffer[String], // list of names (corresponding to IDs), and then "zombie"
  barricadeUrl: String, // randomised per map.cgi hit
  moveUrls: ListBuffer[String] // randomised per map.cgi hit
  ) {

}
