package com.archmage.ghostmind.view

import scalafx.Includes._
import com.archmage.ghostmind.model.{Block, Suburb}
import scalafx.beans.property.{BooleanProperty, StringProperty}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox

class MapBox extends VBox {

  alignment = Pos.TopCenter
  padding = Insets(5)
  spacing = 10

  var baseSuburbText = StringProperty.apply("Suburb")
  var suburbHoverName = StringProperty.apply("Suburb")
  var suburbHover = BooleanProperty.apply(false)

  val searchField = new GhostField {
    promptText = "search"
  }

  val blockName = new Label {
    id = "BoxHeading"
    text = "Block Name"
  }
  val blockGrid: MapGridView = new MapGridView(Block.blocks, 100, (_, _) => (), () => ())

  val suburbName = new Label {
    id = "BoxHeading"
    text <== when (suburbHover) choose suburbHoverName otherwise baseSuburbText
  }
  val suburbGrid = new MapGridView(Suburb.suburbs, 10, (x, y) => {
    blockGrid.offsetX.value = x * 10
    blockGrid.offsetY.value = y * 10
    suburbHoverName.value = Suburb.suburbs.lift(x + y * 10).getOrElse(Suburb.default).name
    suburbHover.value = true
  }, () => {
    suburbHoverName.value = "Suburb"
    suburbHover.value = false
  })

  suburbGrid.alignment <== alignment
  blockGrid.alignment <== alignment

  searchField.text.onChange { (_, _, newValue) => {
    var matchCount = 0
    var lastMatch = ""
    suburbGrid.cells.foreach { cell =>
      val doesMatch = !newValue.isEmpty && cell.dataSource.getName.toLowerCase.contains(newValue.toLowerCase)
      cell.selected.value = doesMatch
      if(doesMatch) {
        matchCount += 1
        lastMatch = cell.dataSource.getName
      }
    }
    baseSuburbText.value = matchCount match {
      case 0 => "Suburb"
      case 1 => lastMatch
      case _ => s"$matchCount matches"
    }
  }}

  children = List(searchField, suburbName, suburbGrid, blockName, blockGrid)
}
