package com.archmage.ghostmind.model

import net.ruippeixotog.scalascraper.model.Element

import scala.collection.mutable.ListBuffer

object MapData {
  def parseResponse(response: Element): MapData = {
    MapData(response, "", 0, 0, 0, 0, None, None, None, "", ListBuffer(), "", "", 0, ListBuffer(), ListBuffer(),
      ListBuffer(), 0, ListBuffer(), ListBuffer(), "", ListBuffer())
  }
}

// rationalisable form of map.cgi responses
case class MapData (
  response: Element,
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
