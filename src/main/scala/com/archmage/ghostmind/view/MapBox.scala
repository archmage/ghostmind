package com.archmage.ghostmind.view

import scalafx.geometry.Pos
import scalafx.scene.layout.VBox

class MapBox extends VBox {

  alignment = Pos.TopCenter
  spacing = 10

  val searchField = new GhostField
  val suburbGrid = new MapGridView
  val blockGrid = new MapGridView

  suburbGrid.alignment <== alignment
  blockGrid.alignment <== alignment

  searchField.promptText = "search"

  children = List(searchField, suburbGrid, blockGrid)
}
