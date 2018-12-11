package com.archmage.ghostmind.view

import scalafx.Includes._
import com.archmage.ghostmind.model.Suburb
import scalafx.beans.property.{BooleanProperty, IntegerProperty, ObjectProperty}
import scalafx.scene.layout.{ColumnConstraints, GridPane, RowConstraints}
import scalafx.scene.shape.Rectangle

import scala.collection.mutable.ListBuffer

object MapGridView {
  val cellSize = 12

  val gridWidth = 10
  val gridHeight = 10

  val rowConstraint: RowConstraints = new RowConstraints { prefHeight = MapGridView.cellSize + 1 }
  val columnConstraint: ColumnConstraints = new ColumnConstraints { prefWidth = MapGridView.cellSize + 1 }
}

class MapGridView(
  val dataSources: List[MapGridViewDataSource],
  val dataSourceWidth: Int,
  onHoverEnter: (Int, Int) => Unit,
  onHoverExit: () => Unit) extends GridPane {

  var offsetX: IntegerProperty = IntegerProperty.apply(0)
  var offsetY: IntegerProperty = IntegerProperty.apply(0)

  offsetX.onChange { (_, oldValue, newValue) => {
    println(s"x changed from $oldValue to $newValue")
    update()
  }}
  offsetY.onChange { (_, oldValue, newValue) => {
    println(s"y changed from $oldValue to $newValue")
    update()
  }}

  rowConstraints = List.fill(MapGridView.gridHeight)(MapGridView.rowConstraint)
  columnConstraints = List.fill(MapGridView.gridWidth)(MapGridView.columnConstraint)

  onMouseExited = _ => onHoverExit()

  var cells = ListBuffer[MapGridRectangle]()

  for(cellX <- 0 until MapGridView.gridWidth) {
    for (cellY <- 0 until MapGridView.gridHeight) {
      val dataSource = dataSources.lift.apply(
        (cellX + offsetX.value) + dataSourceWidth * (cellY + offsetY.value)).getOrElse(Suburb.default)
      val cell = new MapGridRectangle(dataSource) {
        width = MapGridView.cellSize
        height = MapGridView.cellSize
        onMouseEntered = _ => onHoverEnter(cellX + offsetX.value, cellY + offsetY.value)
      }

      this.add(cell, cellX, cellY)
      cells += cell
    }
  }

  def update(): Unit = {
    cells.zipWithIndex.foreach { cellWithIndex =>
      val cellX = cellWithIndex._2 % 10
      val cellY = cellWithIndex._2 / 10
      val newDataSourceValue = dataSources.lift.apply(
        (cellX + offsetX.value) + dataSourceWidth * (cellY + offsetY.value)).getOrElse(Suburb.default)
      cellWithIndex._1.dataSource.value = newDataSourceValue
    }
  }
}

object MapGridRectangle {
  val selectedStyle = "-fx-stroke: black; -fx-stroke-width: 2; -fx-stroke-type: inside;"
}

class MapGridRectangle(dataSourceValue: MapGridViewDataSource) extends Rectangle {
  val dataSource: ObjectProperty[MapGridViewDataSource] = ObjectProperty.apply(dataSourceValue)
  var selected = BooleanProperty.apply(false)

  dataSource.onChange { (_, _, _) =>
    style <== when (selected) choose s"${colourStyle()} ${MapGridRectangle.selectedStyle}" otherwise colourStyle()
  }

  style <== when (selected) choose s"${colourStyle()} ${MapGridRectangle.selectedStyle}" otherwise colourStyle()

  width = MapGridView.cellSize
  height = MapGridView.cellSize

  def colourStyle(): String = s"-fx-fill: ${dataSource.value.colourStyle()};"
}

trait MapGridViewDataSource {
  def getName: String
  def colourStyle(): String
}