package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.{Block, CharacterSession, Suburb}
import scalafx.beans.property.IntegerProperty
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox

class MapBox(val session: CharacterSession) extends VBox {

  alignment = Pos.TopCenter
  padding = Insets(10)
  spacing = 10

  // the suburb for blockGrid to show
  var activeSuburb = IntegerProperty.apply(0)

  // the block for labels and stuff to show
//  var activeBlock = IntegerProperty.apply(0)

  // search stuff
  var suburbMatchCount: Option[Int] = None
  var lastSuburbMatch: Option[Int] = None

  var blockMatchCount: Option[Int] = None
  var lastBlockMatch: Option[Int] = None

  // ui stuff
  val searchField = new GhostField {
    promptText = "search"
  }
  val searchStatusLabel = new Label {
    id = "WhiteText"
    text = ""
    style = "-fx-text-fill: -lighter-grey;"
    margin = Insets(-8, 0, 0, 0)
  }
  val suburbLabel = new Label {
    id = "BoxHeading"
    text = defaultSuburbLabelText
  }
  val blockGrid: MapGridView = new MapGridView(Block.blocks, 100)
  val blockLabel = new Label {
    id = "WhiteText"
    text = defaultBlockLabelText
  }
  val coordinatesLabel = new Label {
    id = "WhiteText"
    text = defaultCoordinatesLabelText
  }
  // buncha closures to do activeSuburb changes
  val suburbGrid: MapGridView = new MapGridView(Suburb.suburbs, 10) {
    onHoverEnter = (_, _) => {
      if(suburbGrid.selectedCell.value.isEmpty) activeSuburb.value = suburbGrid.lastHoveredCell.value.get
    }
    onHoverExit = () => {
      resetActiveSuburb()
    }
    onSelect = (_, _) => {
      activeSuburb.value = suburbGrid.selectedCell.value.get
    }
    onDeselect = () => {
      activeSuburb.value = suburbGrid.lastHoveredCell.value.get
    }
  }

  def init(): Unit = {
    suburbGrid.alignment <== alignment
    blockGrid.alignment <== alignment

    activeSuburb.onChange { (_, _, _) => updateBlockGridOffset() }
//    activeBlock.onChange { (_, _, _) => updateBlockLabels() }

    suburbGrid.lastHoveredCell.onChange { (_, _, _) => update() }
    suburbGrid.selectedCell.onChange { (_, _, _) => update() }
    blockGrid.lastHoveredCell.onChange { (_, _, _) => update() }
    blockGrid.selectedCell.onChange { (_, _, _) => update() }

    searchField.text.onChange { (_, _, newValue) => {
      // reset matches
      suburbMatchCount = None
      if(lastSuburbMatch.isDefined) {
        suburbGrid.cells(lastSuburbMatch.get).highlighted.value = false
        lastSuburbMatch = None
      }
      blockMatchCount = None

      // if search text isn't empty...
      val cleanString = newValue.trim.toLowerCase
      if(!cleanString.isEmpty) {
        // do a suburb search
        suburbMatchCount = Some(0)
        suburbGrid.cells.zipWithIndex.foreach { cell =>
          val doesMatch = cell._1.dataSource.value.getName.toLowerCase.contains(cleanString)
          cell._1.highlighted.value = doesMatch
          if(doesMatch) {
            suburbMatchCount = Some(suburbMatchCount.get + 1)
            lastSuburbMatch = Some(cell._2)
          }
        }
        // if there's only one match, make it the active suburb!
        if(getSingleSuburbMatch.isDefined) {
            activeSuburb.value = lastSuburbMatch.get
        }

        // now do a block search!
        val blockSearchResult = Block.search(cleanString)
        blockMatchCount = Some(blockSearchResult.size)
      }
      else {
        suburbGrid.cells.foreach { cell => cell.highlighted.value = false }
        resetActiveSuburb()
      }
      update()
    }}

    searchField.onAction = _ => {
      if(getSingleSuburbMatch.isDefined) { // select if there's a single match!
        suburbGrid.cells(lastSuburbMatch.get).highlighted.value = false
        suburbGrid.selectCell(lastSuburbMatch.get)
        searchField.text = ""
      }
    }

    resetActiveSuburb()

    children = List(searchField, searchStatusLabel, suburbLabel, suburbGrid, blockLabel, blockGrid, coordinatesLabel)
  }

  def defaultSuburbLabelText: String = {
    val suburbIndex = session.suburbIndex()
    if(suburbIndex.isDefined) Suburb.suburbs(suburbIndex.get).name
    else session.username
  }

  def defaultBlockLabelText: String = {
    if(session.position.isDefined) Block.blocks(session.position.get).name
    else "Block Name"
  }

  def defaultCoordinatesLabelText: String = {
    if(session.position.isDefined) {
      val coordinates = blockGrid.dataSourcesCoordinatesFromIndex(session.position.get)
      s"[${coordinates._1}, ${coordinates._2}]"
    }
    else "[x, y]"
  }

  def getBlockSearchResult: Option[String] = {
    if(blockMatchCount.isDefined) {
      if(blockMatchCount.get == 1) {
        Some("Specific Block!")
      }
      else Some(s"${if(blockMatchCount.get <= 0) "no" else blockMatchCount.get} matches${
        if(blockMatchCount.get > 0) " found" else ""}")
    }
    else None
  }

  def getSingleSuburbMatch: Option[Int] = {
    if(suburbMatchCount.isDefined && suburbMatchCount.get == 1 && lastSuburbMatch.isDefined) lastSuburbMatch
    else None
  }

  def updateBlockGridOffset(): Unit = {
    val (x, y) = MapGridView.cellCoordinatesFromIndex(activeSuburb.value)
    blockGrid.offsetX.value = x * 10
    blockGrid.offsetY.value = y * 10
  }

  def resetActiveSuburb(): Unit = {
    if(suburbGrid.lastHoveredCell.value.isDefined) activeSuburb.value = suburbGrid.lastHoveredCell.value.get
    else if(suburbGrid.selectedCell.value.isDefined) activeSuburb.value = suburbGrid.selectedCell.value.get
    else {
      val suburbIndex = session.suburbIndex()
      if(suburbIndex.isDefined) {
        activeSuburb.value = suburbIndex.get
      }
    }
  }

  def update(): Unit = {
    // update things based on suburb grid changes

//    onHoverEnter = (_, _) => {
//      if(suburbGrid.selectedCell.value.isEmpty) activeSuburb.value = suburbGrid.lastHoveredCell.value.get
//    }
//    onHoverExit = () => {
//      resetActiveSuburb()
//    }
//    onSelect = (_, _) => {
//      activeSuburb.value = suburbGrid.selectedCell.value.get
//    }
//    onDeselect = () => {
//      activeSuburb.value = suburbGrid.lastHoveredCell.value.get
//    }

    // update search status label
    searchStatusLabel.text = {
      if(suburbMatchCount.isEmpty && blockMatchCount.isEmpty) ""
      else {
        s"${
          if(suburbMatchCount.isDefined)
            s"${suburbMatchCount.get} suburb${if (suburbMatchCount.get != 1) "s" else ""}"
          else ""
        }${
          if(suburbMatchCount.isDefined && blockMatchCount.isDefined) ", " else ""
        }${
          if(blockMatchCount.isDefined)
            s"${blockMatchCount.get} block${if (blockMatchCount.get != 1) "s" else ""}"
          else ""
        }"
      }
    }

    // update suburb label text
    suburbLabel.text = {
      // hovered cell
      if(suburbGrid.lastHoveredCell.value.isDefined) {
        suburbGrid.cells(suburbGrid.lastHoveredCell.value.get).dataSource.value.getName
      }
      else {
        // selected cell
        val activeName = suburbGrid.getActiveName
        if(activeName.isDefined) activeName.get
        else defaultSuburbLabelText
      }
    }

    // update block label
    val blockText: Option[String] = {
      val searchResult = getBlockSearchResult
      if(searchResult.isDefined) searchResult
      else blockGrid.getActiveName
    }
    blockLabel.text = if(blockText.isDefined) blockText.get else defaultBlockLabelText
  }

  init()
}
