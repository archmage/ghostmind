package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.Suburb
import javafx.scene.input.MouseButton
import scalafx.beans.property.{BooleanProperty, IntegerProperty, ObjectProperty}
import scalafx.scene.layout.{ColumnConstraints, GridPane, RowConstraints}
import scalafx.scene.shape.Rectangle

import scala.collection.mutable.ListBuffer

object MapGridView {
  val cellSize = 13

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

  var lastHoveredCell: ObjectProperty[Option[MapGridRectangle]] = ObjectProperty.apply(None)
  var selectedCell: ObjectProperty[Option[MapGridRectangle]] = ObjectProperty.apply(None)

  var offsetX: IntegerProperty = IntegerProperty.apply(0)
  var offsetY: IntegerProperty = IntegerProperty.apply(0)

  offsetX.onChange { (_, _, _) => update() }
  offsetY.onChange { (_, _, _) => update() }

  rowConstraints = List.fill(MapGridView.gridHeight)(MapGridView.rowConstraint)
  columnConstraints = List.fill(MapGridView.gridWidth)(MapGridView.columnConstraint)

  onMouseClicked = event => {
    if(event.getButton == MouseButton.PRIMARY) {
      if(lastHoveredCell.value.isDefined) selectCell(lastHoveredCell.value.get)
    }
    else if(event.getButton == MouseButton.SECONDARY) {
      deselectCell()
    }
  }

  onMouseExited = _ => {
    lastHoveredCell.value = None
    onHoverExit()
  }

  var cells = ListBuffer[MapGridRectangle]()

  for (cellY <- 0 until MapGridView.gridHeight) {
    for(cellX <- 0 until MapGridView.gridWidth) {
      val dataSource = dataSources.lift.apply(
        (cellX + offsetX.value) + dataSourceWidth * (cellY + offsetY.value)).getOrElse(Suburb.default)
      val cell = new MapGridRectangle(dataSource) {
        width = MapGridView.cellSize
        height = MapGridView.cellSize
        onMouseEntered = _ => {
          lastHoveredCell.value = Some(this)
          onHoverEnter(cellX + offsetX.value, cellY + offsetY.value)
        }
      }

      this.add(cell, cellX, cellY)
      cells += cell
    }
  }

  def update(): Unit = {
    cells.zipWithIndex.foreach { cellWithIndex =>
      val cellX = cellWithIndex._2 % MapGridView.gridWidth
      val cellY = cellWithIndex._2 / MapGridView.gridHeight
      val newDataSourceValue = dataSources.lift.apply(
        (cellX + offsetX.value) + dataSourceWidth * (cellY + offsetY.value)).getOrElse(Suburb.default)
      cellWithIndex._1.dataSource.value = newDataSourceValue
    }
  }

  def selectCell(cell: MapGridRectangle): Unit = {
    if(selectedCell.value.isDefined) selectedCell.value.get.selected.value = false
    cell.selected.value = true
    selectedCell.value = Some(cell)
  }

  def deselectCell(): Unit = {
    if(selectedCell.value.isDefined) selectedCell.value.get.selected.value = false
    selectedCell.value = None
  }

  def getActiveName(): Option[String] = {
    if(lastHoveredCell.value.isDefined) Some(lastHoveredCell.value.get.dataSource.value.getName)
    else if(selectedCell.value.isDefined) Some(selectedCell.value.get.dataSource.value.getName)
    else None
  }
}

object MapGridRectangle {
  val highlightedStyle = "-fx-stroke: black; -fx-stroke-width: 2; -fx-stroke-type: inside;"
  val selectedStyle = "-fx-stroke: purple; -fx-stroke-width: 2; -fx-stroke-type: inside;"
}

class MapGridRectangle(dataSourceValue: MapGridViewDataSource) extends Rectangle {
  val dataSource: ObjectProperty[MapGridViewDataSource] = ObjectProperty.apply(dataSourceValue)
  var highlighted = BooleanProperty.apply(false)
  var selected = BooleanProperty.apply(false)

  width = MapGridView.cellSize
  height = MapGridView.cellSize

  def updateStyling(): Unit = {
    style = {
      if(selected.value) s"${colourStyle()} ${MapGridRectangle.selectedStyle}"
      else if(highlighted.value) s"${colourStyle()} ${MapGridRectangle.highlightedStyle}"
      else colourStyle()
    }
  }

  dataSource.onChange { (_, _, _) => updateStyling() }
  highlighted.onChange { (_, _, _) => updateStyling() }
  selected.onChange { (_, _, _) => updateStyling() }
  updateStyling()

  def colourStyle(): String = s"-fx-fill: ${dataSource.value.colourStyle()};"
}

trait MapGridViewDataSource {
  def getName: String
  def colourStyle(): String
}
