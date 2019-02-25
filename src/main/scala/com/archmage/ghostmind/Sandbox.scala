package com.archmage.ghostmind

import com.archmage.ghostmind.model.{Block, Suburb, UrbanDeadModel}

object Sandbox extends App {
  Suburb.loadDangerMap()

//  Block.blocks.sortBy(block => block.name.length).slice(0, 10).foreach { block =>
//    println(s"${block.name} (${block.name.length}) [${block.x}, ${block.y}]")
//  }
//  println(Block.blocks.count(block => block.name.contains(",")))
}