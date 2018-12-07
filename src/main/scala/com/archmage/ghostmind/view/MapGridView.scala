package com.archmage.ghostmind.view

import scalafx.Includes._
import com.archmage.ghostmind.model.Suburb
import scalafx.beans.property.BooleanProperty
import scalafx.scene.layout.{ColumnConstraints, GridPane, RowConstraints}
import scalafx.scene.shape.Rectangle

import scala.collection.mutable.ListBuffer

object MapGridView {
  val cellSize = 12

  val rowConstraint: RowConstraints = new RowConstraints { prefHeight = MapGridView.cellSize + 1 }
  val columnConstraint: ColumnConstraints = new ColumnConstraints { prefWidth = MapGridView.cellSize + 1 }
}

class MapGridView(
  val dataSource: List[Suburb],
  onHoverEnter: (Int, Int) => Unit,
  onHoverExit: () => Unit) extends GridPane {

  rowConstraints = List.fill(10)(MapGridView.rowConstraint)
  columnConstraints = List.fill(10)(MapGridView.columnConstraint)

  onMouseExited = _ => onHoverExit()

  var cells = ListBuffer[MapGridRectangle]()

  for(cellX <- 0 to 9) {
      for (cellY <- 0 to 9) {
        val suburb = dataSource.lift.apply(cellX + 10 * cellY).getOrElse(Suburb("Blankburg"))
        val cell = new MapGridRectangle(suburb) {
          width = MapGridView.cellSize
          height = MapGridView.cellSize
          onMouseEntered = _ => onHoverEnter(cellX, cellY)
        }

        this.add(cell, cellX, cellY)
        cells += cell
      }
  }
}

object MapGridRectangle {
  val selectedStyle = "-fx-stroke: black; -fx-stroke-width: 2; -fx-stroke-type: inside;"
}

class MapGridRectangle(val suburb: Suburb) extends Rectangle with MapGridViewDataSource {
  var selected = BooleanProperty.apply(false)
  style <== when (selected) choose s"${colourStyle()} ${MapGridRectangle.selectedStyle}" otherwise colourStyle()
//  style <== when (selected) choose s"-fx-fill: pink;" otherwise colourStyle()
  width = MapGridView.cellSize
  height = MapGridView.cellSize

  def colourStyle(): String = s"-fx-fill: ${suburb.colourStyle()};"
}

trait MapGridViewDataSource {
  def colourStyle(): String
}