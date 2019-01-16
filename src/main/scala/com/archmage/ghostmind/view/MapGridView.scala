package com.archmage.ghostmind.view

import javafx.scene.input.MouseButton
import scalafx.beans.property.{BooleanProperty, IntegerProperty, ObjectProperty}
import scalafx.scene.effect.ColorAdjust
import scalafx.scene.layout.{ColumnConstraints, GridPane, RowConstraints}
import scalafx.scene.shape.Rectangle

import scala.collection.mutable.ListBuffer

object MapGridView {
  val cellSize = 12

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
  // the datasources index of the "default" datasource
  var defaultSource: ObjectProperty[Option[Int]] = ObjectProperty.apply(None)

  // x and y offsets for the data source
  var offsetX: IntegerProperty = IntegerProperty.apply(0)
  var offsetY: IntegerProperty = IntegerProperty.apply(0)

  // a predicate to determine whether a cell should be highlighted
  var highlightPredicate: ObjectProperty[Option[MapGridViewDataSource => Boolean]] = ObjectProperty.apply(None)

  // the number of datasource matches for the predicate
  private var _matchCount: Option[Int] = None
  def matchCount: Option[Int] = _matchCount

  // a single datasource index value if there's only one match from the predicate
  private var _singleMatch: Option[Int] = None
  def singleMatch: Option[Int] = _singleMatch

  def init(): Unit = {
    offsetX.onChange { (_, _, _) => update() }
    offsetY.onChange { (_, _, _) => update() }

    // this is kind of bad, but whatever
    defaultSource.onChange { (_, _, _) => update() }

    highlightPredicate.onChange { (_, _, _) => updateHighlights() }

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
    }

    // setup cells
    for (cellY <- 0 until MapGridView.gridHeight) {
      for(cellX <- 0 until MapGridView.gridWidth) {
        val dataSource = dataSources.lift.apply(
          (cellX + offsetX.value) + dataSourceWidth * (cellY + offsetY.value)).get
        val cell = new MapGridCell(dataSource) {
          width = MapGridView.cellSize
          height = MapGridView.cellSize
          onMouseEntered = _ => { lastHoveredCell.value = Some(cellX + MapGridView.gridWidth * cellY) }
        }

        this.add(cell, cellX, cellY)
        cells += cell
      }
    }
  }

  // update data sources for cells, and highlights
  def update(): Unit = {
    cells.zipWithIndex.foreach { cellWithIndex =>
      val (cellX, cellY) = MapGridView.cellCoordinatesFromIndex(cellWithIndex._2)
      val newDataSourceValue = dataSources.lift.apply(
        (cellX + offsetX.value) + dataSourceWidth * (cellY + offsetY.value)).get
      cellWithIndex._1.dataSource.value = newDataSourceValue
      cellWithIndex._1.defaultSource.value = defaultSource.value.isDefined &&
        dataSourceIndexFromCoordinates(cellX + offsetX.value, cellY + offsetY.value) == defaultSource.value.get
    }

    updateHighlights()
  }

  // update highlights and match count based off the predicate!
  def updateHighlights(): Unit = {
    if(highlightPredicate.value.isEmpty) {
      cells.foreach { _.highlighted.value = None }
      _matchCount = None
      _singleMatch = None
    }
    else {
      cells.foreach { cell => cell.highlighted.value = Some(highlightPredicate.value.get.apply(cell.dataSource.value)) }
      val matches = dataSources.zipWithIndex.filter {
        dataSourceWithIndex => highlightPredicate.value.get.apply(dataSourceWithIndex._1)
      }
      _matchCount = Some(matches.size)
      _singleMatch = if(matches.size == 1) Some(matches.head._2) else None
    }
  }

  // reset highlighting
  def resetHighlights(): Unit = {
    highlightPredicate.value = None
    _matchCount = None
    _singleMatch = None
  }

  def dataSourcesCoordinatesFromIndex(index: Int): (Int, Int) = {
    (index % dataSourceWidth, index / dataSourceWidth)
  }

  def dataSourceIndexFromCoordinates(x: Int, y: Int): Int = {
    x + dataSourceWidth * y
  }

  def selectCell(index: Int): Unit = {
    if(selectedCell.value.isDefined) cells(selectedCell.value.get).selected.value = false
    cells(index).selected.value = true
    selectedCell.value = Some(index)
  }

  def deselectCell(): Unit = {
    if(selectedCell.value.isDefined) cells(selectedCell.value.get).selected.value = false
    selectedCell.value = None
  }

//  def getActiveName: Option[String] = {
//    if(lastHoveredCell.value.isDefined) Some(cells(lastHoveredCell.value.get).dataSource.value.getName)
//    else if(singleMatch.isDefined) Some(dataSources(singleMatch.get).getName)
//    else if(selectedCell.value.isDefined) Some(cells(selectedCell.value.get).dataSource.value.getName)
//    else None
//  }

  init()
}

object MapGridCell {
  val defaultSourceStyle = "-fx-stroke: white; -fx-stroke-width: 2; -fx-stroke-dash-array: 2 1 2 1; -fx-stroke-type: inside;"
  val highlightedStyle = "-fx-stroke: #e5b244; -fx-stroke-width: 1; -fx-stroke-type: inside;"
  val selectedStyle = "-fx-stroke: purple; -fx-stroke-width: 1; -fx-stroke-type: inside;"

  val darken = new ColorAdjust
  darken.brightness = -0.7
  darken.saturation = -0.7
}

class MapGridCell(dataSourceValue: MapGridViewDataSource) extends Rectangle {
  val dataSource: ObjectProperty[MapGridViewDataSource] = ObjectProperty.apply(dataSourceValue)
  var highlighted: ObjectProperty[Option[Boolean]] = ObjectProperty.apply(None)
  var selected = BooleanProperty.apply(false)
  var defaultSource = BooleanProperty.apply(false)

  width = MapGridView.cellSize
  height = MapGridView.cellSize

  def updateStyling(): Unit = {
    style = {
      if(selected.value) s"${colourStyle()} ${MapGridCell.selectedStyle}"
      else if(highlighted.value.isDefined && highlighted.value.get) s"${colourStyle()} ${MapGridCell.highlightedStyle}"
      else if(defaultSource.value) s"${colourStyle()} ${MapGridCell.defaultSourceStyle}"
      else colourStyle()
    }

    if(highlighted.value.isDefined) {
      effect = if(highlighted.value.get) null else MapGridCell.darken
    }
    else effect = null
  }

  dataSource.onChange { (_, _, _) => updateStyling() }
  highlighted.onChange { (_, _, _) => updateStyling() }
  selected.onChange { (_, _, _) => updateStyling() }
  defaultSource.onChange { (_, _, _) => updateStyling() }
  updateStyling()

  def colourStyle(): String = s"-fx-fill: ${dataSource.value.colourStyle()};"
}
