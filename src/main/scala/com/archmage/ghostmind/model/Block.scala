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
        blocks.foreach { block =>
          block.danger = Some(DangerLevel.values(Constants.rng.nextInt(DangerLevel.values.length)))
        }
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

case class Block(name: String) extends MapGridViewDataSource {
  var danger: Option[DangerLevel] = Some(Moderate)

  override def getName: String = name

  override def colourStyle(): String =
    s"-suburb-${danger.getOrElse(Abandoned).toString.toLowerCase}"
}

