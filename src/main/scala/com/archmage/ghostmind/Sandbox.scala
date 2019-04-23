package com.archmage.ghostmind

import com.archmage.ghostmind.model.{Block, Suburb, UrbanDeadModel}
import com.archmage.ghostmind.view.StatusBar

import scala.collection.mutable.ListBuffer

object Sandbox extends App {

  val darkList = List("bank", "cinema", "club")

  val fuelDepotPredicate: List[Block] => Boolean = blocks => {
    val factoryCount = blocks.count(_.blockType.contains("factory"))
    val autoRepairCount = blocks.count(_.blockType.contains("autorepair"))
    val darkCount = blocks.count(block => darkList.contains(block.blockType.getOrElse("")))

    factoryCount >= 1 && factoryCount + autoRepairCount >= 3 && darkCount >= 1
  }

  def findAllAreasMatchingPredicate(predicate: List[Block] => Boolean) = {
    var matches = ListBuffer[List[Block]]()
    for(x <- 1 to 98) for(y <- 1 to 98) {
      val blockIndex = x + y * 100
      val surroundings = List(
        Block.blocks(blockIndex - 101), Block.blocks(blockIndex - 100), Block.blocks(blockIndex - 99),
        Block.blocks(blockIndex - 1), Block.blocks(blockIndex), Block.blocks(blockIndex + 1),
        Block.blocks(blockIndex + 99), Block.blocks(blockIndex + 100), Block.blocks(blockIndex + 101)
      )
      if(predicate(surroundings)) matches += surroundings
    }

    var dotMatrix: ListBuffer[StringBuilder] = ListBuffer.fill(100)(new StringBuilder("." * 100))
    val symbols = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"

    matches.zipWithIndex.foreach { surroundingsZip =>
      val suburbs = surroundingsZip._1.map(block => Suburb.suburbs(block.getSuburbIndex)).distinct
      val commaSeparated = suburbs.foldLeft("")((acc, i) => acc + i.name + ", ").dropRight(2)
      println(s"${symbols(surroundingsZip._2)}. $commaSeparated")
      surroundingsZip._1.foreach { block =>
        dotMatrix(block.y)(block.x) = symbols(surroundingsZip._2)
        println(s"${block.name} [${block.x}, ${block.y}]")
      }
      println("")
    }

    dotMatrix.foreach { line => println(line.toString()) }

    println(s"\n${matches.size} matches found.")
  }

  println("Matching off predicate: factoryCount >= 1 && factoryCount + autoRepairCount >= 3 && darkCount >= 1")
  println("Searching dimensions: 3x3\n")

  findAllAreasMatchingPredicate(fuelDepotPredicate)

  def tryUsingModelWithoutUI(): Unit = {
    println(UrbanDeadModel.loadCharacters())
    println(UrbanDeadModel.sessions)

    UrbanDeadModel.sessions.flatten.headOption.map {
      head => UrbanDeadModel.loginExistingSession(head, UrbanDeadModel.sessions.indexOf(Some(head)))
    }
  }
}