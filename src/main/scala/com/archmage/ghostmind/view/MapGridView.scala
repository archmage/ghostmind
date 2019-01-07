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

  def cellCoordinatesFromIndex(index: Int): (Int, Int) = {
    (index % MapGridView.gridWidth, index / MapGridView.gridHeight)
  }
}

trait MapGridViewDataSource {
  def getName: String
  def colourStyle(): String
}

class MapGridView(
  val dataSources: List[MapGridViewDataSource],
  val dataSourceWidth: Int) extends GridPane {

  // list of cells; should be size() == 100
  var cells = ListBuffer[MapGridCell]()

  // the index (0-99) of the last cell that was hovered
  var lastHoveredCell: ObjectProperty[Option[Int]] = ObjectProperty.apply(None)
  // the index (0-99) of the currently selected cell
  var selectedCell: ObjectProperty[Option[Int]] = ObjectProperty.apply(None)

  // x and y offsets for the data source
  var offsetX: IntegerProperty = IntegerProperty.apply(0)
  var offsetY: IntegerProperty = IntegerProperty.apply(0)

  // a predicate to determine whether a cell should be highlighted
  var highlightPredicate: MapGridViewDataSource => Boolean = { _ => false }

  var onHoverEnter: (Int, Int) => Unit = (_, _) => ()
  var onHoverExit: () => Unit = () => ()
  var onSelect: (Int, Int) => Unit = (_, _) => ()
  var onDeselect: () => Unit = () => ()

  def init(): Unit = {
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

    // setup cells
    for (cellY <- 0 until MapGridView.gridHeight) {
      for(cellX <- 0 until MapGridView.gridWidth) {
        val dataSource = dataSources.lift.apply(
          (cellX + offsetX.value) + dataSourceWidth * (cellY + offsetY.value)).getOrElse(Suburb.default)
        val cell = new MapGridCell(dataSource) {
          width = MapGridView.cellSize
          height = MapGridView.cellSize
          onMouseEntered = _ => {
            lastHoveredCell.value = Some(cellX + MapGridView.gridWidth * cellY)
            onHoverEnter(cellX + offsetX.value, cellY + offsetY.value)
          }
        }

        this.add(cell, cellX, cellY)
        cells += cell
      }
    }
  }

  def update(): Unit = {
    deselectCell()
    cells.zipWithIndex.foreach { cellWithIndex =>
      val (cellX, cellY) = MapGridView.cellCoordinatesFromIndex(cellWithIndex._2)
      val newDataSourceValue = dataSources.lift.apply(
        (cellX + offsetX.value) + dataSourceWidth * (cellY + offsetY.value)).getOrElse(Suburb.default)
      cellWithIndex._1.dataSource.value = newDataSourceValue
      cellWithIndex._1.highlighted.value = highlightPredicate(cellWithIndex._1.dataSource.value)
    }
  }

  def dataSourcesCoordinatesFromIndex(index: Int): (Int, Int) = {
    (index % dataSourceWidth, index / dataSourceWidth)
  }

  def selectCell(index: Int): Unit = {
    if(selectedCell.value.isDefined) cells(selectedCell.value.get).selected.value = false
    cells(index).selected.value = true
    selectedCell.value = Some(index)
    val (cellX, cellY) = MapGridView.cellCoordinatesFromIndex(index)
    onSelect(cellX, cellY)
  }

  def deselectCell(): Unit = {
    if(selectedCell.value.isDefined) cells(selectedCell.value.get).selected.value = false
    selectedCell.value = None
    onDeselect()
  }

  def getActiveName: Option[String] = {
    if(lastHoveredCell.value.isDefined) Some(cells(lastHoveredCell.value.get).dataSource.value.getName)
    else if(selectedCell.value.isDefined) Some(cells(selectedCell.value.get).dataSource.value.getName)
    else None
  }

  init()
}

object MapGridCell {
  val currentStyle = "-fx-stroke: white; -fx-stroke-width: 2; -fx-stroke-type: inside;"
  val highlightedStyle = "-fx-stroke: black; -fx-stroke-width: 2; -fx-stroke-type: inside;"
  val selectedStyle = "-fx-stroke: purple; -fx-stroke-width: 2; -fx-stroke-type: inside;"
}

class MapGridCell(dataSourceValue: MapGridViewDataSource) extends Rectangle {
  val dataSource: ObjectProperty[MapGridViewDataSource] = ObjectProperty.apply(dataSourceValue)
  var highlighted = BooleanProperty.apply(false)
  var selected = BooleanProperty.apply(false)

  width = MapGridView.cellSize
  height = MapGridView.cellSize

  def updateStyling(): Unit = {
    style = {
      if(selected.value) s"${colourStyle()} ${MapGridCell.selectedStyle}"
      else if(highlighted.value) s"${colourStyle()} ${MapGridCell.highlightedStyle}"
      else colourStyle()
    }
  }

  dataSource.onChange { (_, _, _) => updateStyling() }
  highlighted.onChange { (_, _, _) => updateStyling() }
  selected.onChange { (_, _, _) => updateStyling() }
  updateStyling()

  def colourStyle(): String = s"-fx-fill: ${dataSource.value.colourStyle()};"
}
