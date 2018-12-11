package com.archmage.ghostmind.model

import java.io.File

import com.archmage.ghostmind.view.MapGridViewDataSource
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse

import scala.io.Source

object Block {
  implicit val formats = DefaultFormats
  val blocksFile = "blocks.json"

  val blocks: List[Block] = {
    val file = new File(blocksFile)
    if(file.exists()) {
      val stream = Source.fromFile(file)
      val string = stream.getLines.mkString
      if(!string.isEmpty) {
        val parsed = parse(string)
        val blocks = (parsed \\ "blocks").extract[List[Block]]
        stream.close()
        blocks
      }
      else {
        stream.close()
        List[Block]()
      }
    }
    else List[Block]()
  }
}

case class Block(x: Int, y: Int, name: String, var blockType: Option[String]) extends MapGridViewDataSource {
  override def getName: String = name

  override def colourStyle(): String =
    s"-block-${blockType.getOrElse("building").toLowerCase}"
}
/*
sealed class BlockType extends SomeStrat {
  override def style(): String = "blah"
}
// buildings
//case object Cathedral extends BlockType
//case object ChurchLarge extends BlockType
//case object Mall extends BlockType
//case object Mansion extends BlockType
//case object PowerStation extends BlockType
//case object Stadium extends BlockType
//case object Fort extends BlockType
//case object ZooEnclosure extends BlockType
//case object AutoRepair extends BlockType
//case object Bank extends BlockType
case object Building extends BlockType {
  override def style(): String = "building"
}
//case object NecroTech extends BlockType
//case object Church extends BlockType
//case object Cinema extends BlockType
//case object Club extends BlockType
//case object Factory extends BlockType
//case object FireStation extends BlockType
//case object Hospital extends BlockType
//case object Hotel extends BlockType
//case object Junkyard extends BlockType
//case object Library extends BlockType
//case object Museum extends BlockType
//case object PoliceDepartment extends BlockType
//case object Pub extends BlockType
//case object RailwayStation extends BlockType
//case object School extends BlockType
//case object Tower extends BlockType
//case object Warehouse extends BlockType

// outdoors
case object Carpark extends BlockType {
  override def style(): String = "carpark"
}
case object Cemetery extends BlockType {
  override def style(): String = "cemetery"
}
case object Zoo extends BlockType {
  override def style(): String = "zoo"
}
//case object ExerciseYard extends BlockType
case object Monument extends BlockType {
  override def style(): String = "monument"
}
case object Park extends BlockType {
  override def style(): String = "park"
}
case object Street extends BlockType {
  override def style(): String = "street"
}
//case object TrainingGround extends BlockType
case object Wasteland extends BlockType {
  override def style(): String = "wasteland"
}

trait SomeStrat {
  def style(): String
}
*/