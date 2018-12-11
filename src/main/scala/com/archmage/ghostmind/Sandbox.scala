package com.archmage.ghostmind

object Sandbox extends App {
//  println(Suburb.suburbs)
//  println(Block.blocks)

  print("{\"blocks\":[")
  for(i <- 1 to 10000) print(s"""{"name":"The Block $i"},""")
  print("]}")
}