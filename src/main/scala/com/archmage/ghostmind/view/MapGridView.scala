package com.archmage.ghostmind.view

import scalafx.Includes._
import scalafx.scene.layout.{ColumnConstraints, GridPane, RowConstraints}
import scalafx.scene.shape.Rectangle

object MapGridView {
  val cellSize = 11

  val rowConstraint: RowConstraints = new RowConstraints { prefHeight = MapGridView.cellSize + 1 }
  val columnConstraint: ColumnConstraints = new ColumnConstraints { prefWidth = MapGridView.cellSize + 1 }
}

class MapGridView extends GridPane {
  rowConstraints = List.fill(10)(MapGridView.rowConstraint)
  columnConstraints = List.fill(10)(MapGridView.columnConstraint)

  for(x <- 0 to 9) {
      for (y <- 0 to 9) {
        val cell = new Rectangle {
          width = MapGridView.cellSize
          height = MapGridView.cellSize
          fill <== when(hover) choose Colors.green otherwise Colors.red
        }

        this.add(cell, x, y)
      }
  }
}
