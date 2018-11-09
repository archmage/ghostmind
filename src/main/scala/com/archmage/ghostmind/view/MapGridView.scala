package com.archmage.ghostmind.view

import scalafx.Includes._
import scalafx.scene.layout.{ColumnConstraints, GridPane, RowConstraints}
import scalafx.scene.shape.Rectangle

object MapGridView {
  val rowConstraint: RowConstraints = new RowConstraints { prefHeight = 10 }
  val columnConstraint: ColumnConstraints = new ColumnConstraints { prefWidth = 10 }
}

class MapGridView extends GridPane {
  rowConstraints = List.fill(10)(MapGridView.rowConstraint)
  columnConstraints = List.fill(10)(MapGridView.columnConstraint)

  for(x <- 0 to 9) {
      for (y <- 0 to 9) {
        val cell = new Rectangle {
          width = 9
          height = 9
          fill <== when(hover) choose Colors.green otherwise Colors.red
        }
//        val cell = new Label {
//          text = s"[$x, $y]"
//        }
        this.add(cell, x, y)
      }
  }
}
