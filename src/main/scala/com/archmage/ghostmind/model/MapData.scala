package com.archmage.ghostmind.model

import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.{Document, Element}

/**
  * the logic for constructing a MapData instance
  */
// TODO find a better flow for handling cases where `metadata.asleep` causes None to be returned
object MapData {

  val inventoryActionRegex = """\?use-""".r.unanchored
  val encumbranceRegex = """You are ([0-9]+)% encumbered\.""".r.unanchored
  val itemAdditionalDataRegex = """(\(.+?\))""".r.unanchored

  val sleepingSurvivorMessage = "Exhausted, you can go no further."
  val sleepingZombieMessage = "The exertions of the day have numbed your clouded brain. You stand where you were, swaying slightly."

  def parseResponse(response: Document): MapData = {
    val metadata = parseMetadata(response)
    val mapBlock = parseMapBlock(response)

    MapData(
      metadata,
      mapBlock,
      parseStatBlock(response),
      parseEnvironmentBlock(response, metadata),
      parseEventBlock(response, mapBlock.position),
      parseActionBlock(response),
      parseInventoryBlock(response, metadata)
      )
  }

  def parseMetadata(doc: Document): Metadata = {
    val sleepElement = doc >> element("table .c") >/~ validator(element("td"))(_.text == "You are asleep.")
    val asleep = sleepElement.isRight
    Metadata(asleep)
  }

  def parseMapBlock(doc: Document): MapBlock = {
    // <table class="c">
    val block: Element = doc >> element("table .c")

    // parse position from 3x3 map
    val position: Option[Int] = try {
      val centreRow = (block >> elementList("tr"))(2)
      val inputs = centreRow >> elementList("input")
      val hiddenInputs = inputs.filter(_.attr("type") == "hidden")

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
      case _: IndexOutOfBoundsException =>
        println("this happened")
        None
    }

    // re: parsing the 3x3 map for info...
    // each <td> element (a cell) has some useful data for us:
    //
    // - the hidden <input> element with coordinates (e.g. <input type="hidden" name="v" value="20-82">)
    // - the `class` attribute on the _visible_ element (e.g. "ml" in <input type="submit" class="ml" value="Tompson Mall">)
    // - in particular this `class` attribute shows the condition of the block
    //
    // md = normal
    // ml = lit
    // mr = ruined OR (inside + dark)
    // ?? = lit and ruined

    MapBlock(position)
  }

  def parseStatBlock(doc: Document): StatBlock = {
    // <td class="cp"> >> <div class="gt"> (statblock)
    val statBlock = doc >> element(".cp") >> element(".gt")

    // <td class="cp"> >> <div class="gthome"> (safehouse) - not used atm
    // val safehouseBlock = doc >> element(".cp") >?> element(".gthome")

    var hp: Option[Int] = None
    var xp: Option[Int] = None
    var ap: Int = 0

    val boldElements: List[Element] = statBlock >> elementList("b")

    if(boldElements.length <= 2) ap = boldElements.last.text.toInt
    else {
      val numbers = boldElements.slice(boldElements.size - 3, boldElements.size)
      hp = Some(numbers(0).text.toInt)
      xp = Some(numbers(1).text.toInt)
      ap = numbers(2).text.toInt
    }

    // grab the id too?
    val usernameElement: Element = statBlock >> element("a")
    val username = usernameElement.text
    val id = usernameElement.attr("href").substring(UrbanDeadModel.profileUrl.length).toInt

    StatBlock(username, id, hp, xp, ap)
  }

  def parseEnvironmentBlock(doc: Document, metadata: Metadata): EnvironmentBlock = {
    // <td class="gp"> >> <div class="gt">
    val block: Element = doc >> element(".gp") >> element(".gt")
    val content = if(metadata.asleep ||
                     block.text == sleepingSurvivorMessage ||
                     block.text == sleepingZombieMessage) None else Some(block.text)

    EnvironmentBlock(content)
  }

  def parseEventBlock(doc: Document, position: Option[Int]): EventBlock = {
    val messageElement: Option[Element] = doc >?> element(".gamemessage")
    val message = messageElement.map(_.text)

    val eventsElement = doc >?> element("ul")
    val events = eventsElement.map(_ >> elementList("li"))
    val eventInstances = events.map { _.map { event =>
      Event(
        Event.parseTimeText(event.text),
        event,
        Event.parseEventType(event),
        position)
    }}

    EventBlock(message, eventInstances)
  }

  def parseActionBlock(doc: Document): ActionBlock = {
    // <td class="gp"> -> all <form>s, filtering out `?use-` matches
    val forms = doc >> element(".gp") >> elementList("form")
    val actions = forms.filter(form => inventoryActionRegex.findFirstIn(form.attr("action")).isEmpty)

    // get the labels for each
    val actionLabels = actions.flatMap(_ >?> attr("value")("input"))

    ActionBlock(actionLabels)
  }

  // TODO polish this
  def parseInventoryBlock(doc: Document, metadata: Metadata): InventoryBlock = {
    if(metadata.asleep) return InventoryBlock(None, None)

    // <td class="gp"> -> all <form>s, filtering out `?use-` matches
    val forms = doc >> element(".gp") >> elementList("form")
    val items = forms.filter(form => inventoryActionRegex.findFirstIn(form.attr("action")).isDefined)

    // perhaps do some case class mapping of this, rather than just Element -> String
    val itemLabels = items.map { item =>
      val additionalData = item.text match {
        case itemAdditionalDataRegex(data) => Some(data)
        case _ => None
      }
      s"${item >> attr("value")("input")}${if(additionalData.isDefined) s" ${additionalData.get}" else ""}"
    }

    // extract encumbrance
    val pElementList = doc >> elementList("p")
    val encumbrance = pElementList.map(_.text).collectFirst { case encumbranceRegex(value) => value.toInt }

    InventoryBlock(Some(itemLabels), encumbrance)
  }
}

/** a concise data class containing a structured response from `map.cgi` */
case class MapData(
  metadata: Metadata,
  mapBlock: MapBlock,
  statBlock: StatBlock,
  environmentBlock: EnvironmentBlock,
  eventBlock: EventBlock,
  actionBlock: ActionBlock,
  inventoryBlock: InventoryBlock
)

/** info about the map.cgi hit, not any of its data */
case class Metadata(
  asleep: Boolean = false

)

/** the 3x3 map block used for traversing and positioning */
case class MapBlock( // <table class="c">
  position: Option[Int] = None
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
  content: Option[String]
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
  gameMessage: Option[String], // <p class="gamemessage">
  events: Option[List[Event]]
)

/** the block for action forms */
case class ActionBlock( // <td class="gp"> -> all <form>s, filtering out `?use-` matches
   actions: List[String], // no idea how to represent this
  // targetList: ListBuffer[String], // list of names (corresponding to IDs), and then "zombie"

  // parse each action somehow? not sure how to do this tbh
)

/** the block for inventory items, encumbrance and the drop dropdown */
case class InventoryBlock( // <td class="gp"> -> all <form>s, filtering for `?use-` matches
  inventory: Option[List[String]],
  encumbrance: Option[Int], // <td class="gp"> -> <p> containing text akin to "You are [0-9]+% encumbered."

  // dropList: ListBuffer[String], // list of items you could drop; unsure on this

  // a whole bunch of items! this will take some work
)
