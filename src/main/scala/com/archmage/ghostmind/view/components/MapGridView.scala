package com.archmage.ghostmind.view.components

import javafx.scene.input.MouseButton
import scalafx.Includes.{at, _}
import scalafx.animation.Timeline
import scalafx.beans.property.{BooleanProperty, IntegerProperty, ObjectProperty}
import scalafx.scene.Node
import scalafx.scene.effect.ColorAdjust
import scalafx.scene.layout.{ColumnConstraints, GridPane, RowConstraints, StackPane}
import scalafx.scene.paint.Color
import scalafx.scene.shape.{Circle, Rectangle}

import scala.collection.mutable.ListBuffer
import scala.language.postfixOps

object MapGridView {
  val cellSize = 14

  val gridWidth = 10
  val gridHeight = 10

  val rowConstraint: RowConstraints = new RowConstraints { prefHeight = MapGridView.cellSize + 1 }
  val columnConstraint: ColumnConstraints = new ColumnConstraints { prefWidth = MapGridView.cellSize + 1 }

  // given a cell index (0-99), return that cell's coordinates
  def cellCoordinatesFromIndex(index: Int): (Int, Int) = {
    (index % MapGridView.gridWidth, index / MapGridView.gridHeight)
  }

  // given a cell's coordinates, return a cell index (0-99)
  def cellIndexFromCoordinates(x: Int, y: Int): Int = {
    x + y * MapGridView.gridWidth
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

  // the cell index (0-99) of the last cell that was hovered
  var lastHoveredCell: ObjectProperty[Option[Int]] = ObjectProperty(None)

  // the cell index (0-99) of the currently selected cell
  // TODO change this to be a datasource index
  var selectedCell: ObjectProperty[Option[Int]] = ObjectProperty(None)

  // the datasources index of the "default" datasource
  var defaultSource: ObjectProperty[Option[Int]] = ObjectProperty(None)

  // helpers!
  def lastHoveredDataSourceIndex: Option[Int] = {
    if(lastHoveredCell.value.isDefined) Some(dataSourceIndexFromCellIndex(lastHoveredCell.value.get))
    else None
  }
  def selectedDataSourceIndex: Option[Int] = {
    if(selectedCell.value.isDefined) Some(dataSourceIndexFromCellIndex(selectedCell.value.get))
    else None
  }

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
        // context menu!

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
      val dataSourceIndex = dataSourceIndexFromCellIndex(cellWithIndex._2)
      val newDataSourceValue = dataSources.lift.apply(dataSourceIndex).get
      cellWithIndex._1.dataSource.value = newDataSourceValue
      cellWithIndex._1.defaultSource.value = defaultSource.value.isDefined && dataSourceIndex == defaultSource.value.get
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

  // translates datasource index to datasource xy
  def dataSourcesCoordinatesFromIndex(index: Int): (Int, Int) = {
    (index % dataSourceWidth, index / dataSourceWidth)
  }

  // translates datasource xy to datasource index
  def dataSourceIndexFromCoordinates(x: Int, y: Int): Int = {
    x + dataSourceWidth * y
  }

  // translates cell index to datasource index
  def dataSourceIndexFromCellIndex(cellIndex: Int): Int = {
    val x = offsetX.value + cellIndex % MapGridView.gridWidth
    val y = offsetY.value + cellIndex / MapGridView.gridHeight
    x + y * dataSourceWidth
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

  init()
}

object MapGridCell {
  val defaultSourceStyle = "-fx-stroke: white; -fx-stroke-width: 2; -fx-stroke-dash-array: 2 2 2 2; -fx-stroke-type: inside;"
  val highlightedStyle = "-fx-stroke: #e5b244; -fx-stroke-width: 1; -fx-stroke-type: inside;"
  val selectedStyle = "-fx-stroke: yellow; -fx-stroke-width: 2; -fx-stroke-type: inside;"

  val darken = new ColorAdjust
  darken.brightness = -0.5
  darken.saturation = -0.5
}

class MapGridCell(dataSourceValue: MapGridViewDataSource) extends StackPane {

  val dataSource: ObjectProperty[MapGridViewDataSource] = ObjectProperty.apply(dataSourceValue)
  val highlighted: ObjectProperty[Option[Boolean]] = ObjectProperty.apply(None)
  val selected = BooleanProperty.apply(false)
  val defaultSource = BooleanProperty.apply(false)

  val cell: Rectangle = new Rectangle {
    width = MapGridView.cellSize
    height = MapGridView.cellSize
  }

  var selectedCursorTimeline: Option[Timeline] = None

  def colourStyle(): String = s"-fx-fill: ${dataSource.value.colourStyle()};"

  def updateStyling(): Unit = {
    cell.style = {
//      if(selected.value) s"${colourStyle()} ${MapGridCell.selectedStyle}"
//      else if(highlighted.value.isDefined && highlighted.value.get) s"${colourStyle()} ${MapGridCell.highlightedStyle}"
      colourStyle()
    }

    if(highlighted.value.isDefined) {
      cell.effect = if(highlighted.value.get) null else MapGridCell.darken
    }
    else cell.effect = null

    var childNodes: ListBuffer[Node] = ListBuffer(cell)

    if(selected.value) {
      val selectedCursor = new Rectangle {
        width = MapGridView.cellSize
        height = MapGridView.cellSize
        fill = Color.Transparent
        strokeWidth = 2
      }

      val timeline = new Timeline {
        cycleCount = Timeline.Indefinite
        autoReverse = true
        keyFrames = Seq(
          at(0 s) { selectedCursor.stroke -> Color.gray(0.7) /* tween Interpolator.EaseBoth */ },
          at(0.5 s) { selectedCursor.stroke -> Color.White /* tween Interpolator.EaseBoth */ },
        )
      }
      timeline.play()
      selectedCursorTimeline = Some(timeline)
      childNodes += selectedCursor
    }
    else {
      if(selectedCursorTimeline.isDefined) selectedCursorTimeline.get.stop()
      selectedCursorTimeline = None
    }

    if(defaultSource.value) childNodes += new Circle {
      radius = 5
      style = "-fx-fill: red; -fx-stroke: white; -fx-stroke-width: 1.5; -fx-stroke-type: inside"
    }

    if(highlighted.value.isDefined && highlighted.value.get) childNodes += new Circle {
      radius = 3
      style = "-fx-fill: yellow; -fx-stroke: black; -fx-stroke-width: 1; -fx-stroke-type: outside"
    }

    children = childNodes
  }

  dataSource.onChange { (_, _, _) => updateStyling() }
  highlighted.onChange { (_, _, _) => updateStyling() }
  selected.onChange { (_, _, _) => updateStyling() }
  defaultSource.onChange { (_, _, _) => updateStyling() }
  updateStyling()
}
