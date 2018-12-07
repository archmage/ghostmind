package com.archmage.ghostmind.model

import java.io.File

import com.archmage.ghostmind.view.MapGridViewDataSource
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse

import scala.io.Source

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
        suburbs.foreach { suburb =>
          suburb.danger = Some(DangerLevel.values(Constants.rng.nextInt(DangerLevel.values.length)))
        }
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
}

case class Suburb(name: String) extends MapGridViewDataSource {
  var danger: Option[DangerLevel] = Some(Moderate)

  override def colourStyle(): String =
    s"-suburb-${danger.getOrElse(Abandoned).toString.toLowerCase}"
}

object DangerLevel {
  val values = List(Safe, Intact, Moderate, Dangerous, Critical, Abandoned)
}
sealed class DangerLevel
case object Safe extends DangerLevel
case object Intact extends DangerLevel
case object Moderate extends DangerLevel
case object Dangerous extends DangerLevel
case object Critical extends DangerLevel
case object Abandoned extends DangerLevel
