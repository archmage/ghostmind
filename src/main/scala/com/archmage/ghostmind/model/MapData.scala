package com.archmage.ghostmind.model

import com.archmage.ghostmind.model.UrbanDeadModel.{baseUrl, parseProfile, profileUrl}
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.{Document, Element}
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.{element, elementList}

import scala.collection.mutable.ListBuffer

/**
  * the logic for constructing a MapData instance
  */
object MapData {
  def parseMapBlock(doc: Document): MapBlock = {
    MapBlock(doc.body, None)
  }

  def parseStatBlock(doc: Document): StatBlock = {
    StatBlock(doc.body, "", 0, 0, 0, 0)
  }

  def parseEnvironmentBlock(doc: Document): EnvironmentBlock = {
    EnvironmentBlock(doc.body, "")
  }

  def parseEventBlock(doc: Document): EventBlock = {
    EventBlock(doc.body)
  }

  def parseActionBlock(doc: Document): ActionBlock = {
    ActionBlock(doc.body)
  }

  def parseInventoryBlock(doc: Document): InventoryBlock = {
    InventoryBlock(doc.body, 0)
  }

  def parseResponse(response: Document): MapData = {
    // stuff here eventually

    MapData(
      response,
      parseMapBlock(response),
      parseStatBlock(response),
      parseEnvironmentBlock(response),
      parseEventBlock(response),
      parseActionBlock(response),
      parseInventoryBlock(response)
      )
  }
}

// rationalisable form of map.cgi responses
// TODO add gamemessage
case class MapData(
  response: Document,
  mapBlock: MapBlock,
  statBlock: StatBlock,
  environmentBlock: EnvironmentBlock,
  eventBlock: EventBlock,
  actionBlock: ActionBlock,
  inventoryBlock: InventoryBlock
)

/** the 3x3 map block used for traversing and positioning */
case class MapBlock(
  element: Element, // <table class="c">
  position: Option[Int]
  // inside: Option[Boolean]

  // list each block in a size-9 array
  // each has a name, lit status, ruined status, zombie count
)

/** the statistics block ("You are Username. You have X HP and Y XP. You have Z AP.") */
case class StatBlock(
  element: Element, // <td class="cp"> -> <div class="gt"> (statblock) and <div class="gthome"> (safehouse)
  username: String,
  id: Int,
  hp: Int,
  ap: Int,
  xp: Int
  // ignore safehouse for now, it's niche
)

/** the block telling you what's at your location ("You are at X. Also here is Y.") */
case class EnvironmentBlock(
  element: Element, // <td class="gp"> -> <div class="gt">
  content: String
  // barricadeLevel: Option[Int],
  // locationFlavour: String,
  // occupants: ListBuffer[String], // username, profile ID, contact colour, HP
  // radio: Any, // frequency, condition?
  // generator: Any, // health, powered/unpowered, isRunningLow
  // zombieCount: Int,
  // knownZombies: ListBuffer[String], // prolly same data object as occupants, but without HP

  // this is going to require a LOT of custom parsing
  // iterate on this
)

/** the block for gamemessages and events ("Since your last turn:") */
case class EventBlock(
  element: Element, // <p class="gamemessage">
  // represent "Since your last turn:" here somehow
)

/** the block for action forms */
case class ActionBlock(
  element: Element // <td class="gp"> -> all <form>s, filtering out `?use-` matches
  // actions: ListBuffer[String], // no idea how to represent this
  // targetList: ListBuffer[String], // list of names (corresponding to IDs), and then "zombie"

  // parse each action somehow? not sure how to do this tbh
)

/** the block for inventory items, encumbrance and the drop dropdown */
case class InventoryBlock(
  element: Element, // <td class="gp"> -> all <form>s, filtering for `?use-` matches
  encumbrance: Int // <td class="gp"> -> <p> containing text akin to "You are [0-9]+% encumbered."
  // inventory: ListBuffer[Any], // need an object for items
  // dropList: ListBuffer[String], // list of items you could drop; unsure on this

  // a whole bunch of items! this will take some work
)
