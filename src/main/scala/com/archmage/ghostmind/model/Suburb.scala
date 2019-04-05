package com.archmage.ghostmind.model

import java.io.File
import java.net.UnknownHostException

import com.archmage.ghostmind.view.MapGridViewDataSource
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.Document
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse

import scala.io.Source

// TODO keep a local cache of danger values
object Suburb {
  implicit val formats = DefaultFormats
  val suburbsFile = "suburbs.json"

  val suburbs: List[Suburb] = {
    val file = new File(suburbsFile)
    if(file.exists()) {
      val stream = Source.fromFile(file)
      val string = stream.getLines.mkString
      if(!string.isEmpty) {
        val parsed = parse(string)
        val suburbs = (parsed \\ "suburbs").extract[List[Suburb]]
        stream.close()
        suburbs
      }
      else {
        stream.close()
        List[Suburb]()
      }
    }
    else List[Suburb]()
  }

  val default = Suburb("Blanktown")

  val dangerMapStyleRegex = """background:#([0-9A-F]{3})""".r.unanchored

  def loadDangerMap(): Option[Exception] = {
    val wikiSuburbResponse = try {
      Constants.browser.get("http://wiki.urbandead.com/index.php/Suburb")
    }
    catch {
      case uhe: UnknownHostException => return Some(uhe)
    }

    val table = (wikiSuburbResponse >> elementList("table"))(1) // bad but whatever
    val cells = table >> elementList("td")
    val dangerLevels: List[DangerLevel] = cells.map { cell =>

      val styleText = cell.attr("style")
      val colour = dangerMapStyleRegex.findAllIn(styleText).group(1)
      if(colour.isEmpty) {
        Abandoned
      }
      else colour match {
        case Safe.colourCode => Safe
        case Intact.colourCode => Intact
        case Moderate.colourCode => Moderate
        case Dangerous.colourCode => Dangerous
        case Critical.colourCode => Critical
        case Abandoned.colourCode => Abandoned
        case _ => Abandoned
      }
    }
    for(i <- suburbs.indices) suburbs(i).danger = dangerLevels(i)

    None
  }
}

case class Suburb(name: String) extends MapGridViewDataSource {
  var danger: DangerLevel = Abandoned

  override def getName: String = name

  override def colourStyle(): String =
    s"-suburb-${danger.toString.toLowerCase}"
}

object DangerLevel {
  val values = List(Safe, Intact, Moderate, Dangerous, Critical, Abandoned)
}
sealed abstract class DangerLevel(val colourCode: String)
case object Safe extends DangerLevel("CFC")
case object Intact extends DangerLevel("AED")
case object Moderate extends DangerLevel("FFC")
case object Dangerous extends DangerLevel("FD9")
case object Critical extends DangerLevel("F99")
case object Abandoned extends DangerLevel("EEE")
