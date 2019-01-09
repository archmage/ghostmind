package com.archmage.ghostmind

import com.archmage.ghostmind.model.Block

object Sandbox extends App {
  Block.blocks.sortBy(block => block.name.length).reverse.slice(0, 10).foreach { block =>
    println(s"${block.name} (${block.name.length}) [${block.x}, ${block.y}]")
  }
}