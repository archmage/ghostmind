package com.archmage.ghostmind.model

import java.io.File

import com.archmage.ghostmind.view.components.MapGridViewDataSource
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

  val blankBlock = "------"
  val blankCoordinates = "[--, --]"

  def search(string: String): List[Block] = {
    val cleanString = string.trim.toLowerCase
    blocks.filter(block => block.name.toLowerCase.contains(cleanString))
  }
}

case class Block(x: Int, y: Int, name: String, var blockType: Option[String]) extends MapGridViewDataSource {
  override def getName: String = name

  override def colourStyle(): String =
    s"-block-${blockType.getOrElse("building").toLowerCase}"

  def getSuburbIndex: Int = {
    x / 10 + (y / 10) * 10
  }
}