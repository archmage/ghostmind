package com.archmage.ghostmind.model

import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.{Document, Element}

/**
  * the logic for constructing a MapData instance
  */
object MapData {

  def parseResponse(response: Document): MapData = {
    MapData(
      parseMapBlock(response),
      parseStatBlock(response),
      parseEnvironmentBlock(response),
      parseEventBlock(response),
      parseActionBlock(response),
      parseInventoryBlock(response)
      )
  }

  def parseMapBlock(doc: Document): MapBlock = {
    // <table class="c">
    val block = doc.body >> element("table .c")

    val position: Option[Int] = try {
      val centreRow = (block >> elementList("tr"))(2)
      val inputs = centreRow >> elementList("input")
      val hiddenInputs = inputs.filter(element => element.attr("type") == "hidden")
      val coordinates = hiddenInputs.map { input =>
        val xy = input.attr("value").split("-")
        (xy(0).toInt, xy(1).toInt)
      }
      var x = 0
      var y = 0

      if(coordinates.length == 2) {
        x = coordinates(0)._1 + 1
        y = coordinates(0)._2
      }
      else {
        y = coordinates(0)._2
        if(coordinates(0)._1 == 1) x = 0
        else if(coordinates(0)._1 == 98) x = 99
      }

      Some(x + y * 100)
    }
    catch {
      case _: IndexOutOfBoundsException => {
        println("this happened")
        None
      }
    }

    MapBlock(position)
  }

  def parseStatBlock(doc: Document): StatBlock = {
    // <td class="cp"> -> <div class="gt"> (statblock) and <div class="gthome"> (safehouse)
    val block = doc.body >> element(".cp") >> element(".gt")

    var hp: Option[Int] = None
    var xp: Option[Int] = None
    var ap: Int = 0

    val boldElements = block >> elementList("b")

    if(boldElements.length <= 2) ap = boldElements.last.text.toInt
    else {
      val numbers = boldElements.slice(boldElements.size - 3, boldElements.size)
      hp = Some(numbers(0).text.toInt)
      xp = Some(numbers(1).text.toInt)
      ap = numbers(2).text.toInt
    }

    // grab the id too?
    val usernameElement = block >> element("a")
    val username = usernameElement.text
    val id = usernameElement.attr("href").substring(UrbanDeadModel.profileUrl.length).toInt

    StatBlock(username, id, hp, xp, ap)
  }

  def parseEnvironmentBlock(doc: Document): EnvironmentBlock = {
    EnvironmentBlock("")
  }

  def parseEventBlock(doc: Document): EventBlock = {
    EventBlock(None)
  }

  def parseActionBlock(doc: Document): ActionBlock = {
    ActionBlock()
  }

  def parseInventoryBlock(doc: Document): InventoryBlock = {
    val encumbranceRegex = """You are ([0-9]+)% encumbered\.""".r.unanchored
    val pElementList = doc >> elementList("p")

    // bit overcomplex, but given an indeterminate number of p-elements, this works well
    // maybe simplify it in the future
    val encumbrance = pElementList.foldLeft(0) { (acc, i) => i.text match {
      case encumbranceRegex(value) => value.toInt
      case _ => acc
    }}

    InventoryBlock(encumbrance)
  }
}

/** a concise data class containing a structured response from `map.cgi` */
case class MapData(
  mapBlock: MapBlock,
  statBlock: StatBlock,
  environmentBlock: EnvironmentBlock,
  eventBlock: EventBlock,
  actionBlock: ActionBlock,
  inventoryBlock: InventoryBlock
)

/** the 3x3 map block used for traversing and positioning */
case class MapBlock( // <table class="c">
  position: Option[Int]
  // inside: Option[Boolean]

  // list each block in a size-9 array
  // each has a name, lit status, ruined status, zombie count
)

/** the statistics block ("You are Username. You have X HP and Y XP. You have Z AP.") */
case class StatBlock( // <td class="cp"> >> <div class="gt"> (statblock) and <div class="gthome"> (safehouse)
  username: String,
  id: Int,
  hp: Option[Int],
  xp: Option[Int],
  ap: Int
  // ignore safehouse for now, it's niche
)

/** the block telling you what's at your location ("You are at X. Also here is Y.") */
case class EnvironmentBlock( // <td class="gp"> >> <div class="gt">
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
case class EventBlock( // <p class="gamemessage">
  gameMessage: Option[String]
  // represent "Since your last turn:" here somehow
)

/** the block for action forms */
case class ActionBlock( // <td class="gp"> -> all <form>s, filtering out `?use-` matches
  // actions: ListBuffer[String], // no idea how to represent this
  // targetList: ListBuffer[String], // list of names (corresponding to IDs), and then "zombie"

  // parse each action somehow? not sure how to do this tbh
)

/** the block for inventory items, encumbrance and the drop dropdown */
case class InventoryBlock( // <td class="gp"> -> all <form>s, filtering for `?use-` matches
  encumbrance: Int // <td class="gp"> -> <p> containing text akin to "You are [0-9]+% encumbered."
  // inventory: ListBuffer[Any], // need an object for items
  // dropList: ListBuffer[String], // list of items you could drop; unsure on this

  // a whole bunch of items! this will take some work
)
