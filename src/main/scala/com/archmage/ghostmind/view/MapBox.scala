package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.{Block, CharacterSession, Suburb}
import scalafx.beans.property.{IntegerProperty, ObjectProperty}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox
import scalafx.scene.paint.Color
import scalafx.scene.text.{Text, TextAlignment, TextFlow}

object MapBox {
  val coordinatesRegex = """^([0-9]{1,2}) ([0-9]{1,2})$""".r.unanchored
}

class MapBox(val session: CharacterSession) extends VBox with Updateable {

  alignment = Pos.TopCenter
  padding = Insets(2, 12, 2, 12) // 2, 5, 2, 5
//  maxWidth = 169

  // the suburb for blockGrid to show
  var activeSuburb = IntegerProperty.apply(0)

  // the block to be used for labels
  var activeBlock: ObjectProperty[Option[Int]] = ObjectProperty.apply(None)

  // -- ui stuff --
  val searchField = new GhostField {
    promptText = "search"
    margin = Insets(2, 0, 0, 0)
  }
  val searchStatusLabel = new Label {
    id = "WhiteText"
    text = ""
    style = "-fx-text-fill: -lighter-grey;"
  }
  val suburbLabel = new Label {
    id = "BoxHeading"
    text = getSessionSuburb
    margin = Insets(8, 0, 4, 0)
  }
  val blockGrid: MapGridView = new MapGridView(Block.blocks, 100)
  val blockText = new Text {
    fill = Color.White
    text = getSessionBlock
  }
  val blockLabel = new TextFlow {
    maxWidth = 130
    textAlignment = TextAlignment.Center
    children = blockText
  }
  val blockLabelVBox = new VBox {
    alignment = Pos.BottomCenter
    padding = Insets(0)
    spacing = 0
    minHeight = 32
    margin = Insets(8, 0, 4, 0)
    children = blockLabel
  }
  val coordinatesLabel = new Label {
    id = "WhiteText"
    text = getSessionCoordinates
    margin = Insets(4, 0, 0, 0)
  }
  val coordinatesDeltaLabel = new Label {
    id = "WhiteText"
    text = getSessionCoordinatesDelta
    margin = Insets(2, 0, 0, 0)
  }

  val suburbGrid: MapGridView = new MapGridView(Suburb.suburbs, 10)

  def init(): Unit = {
    suburbGrid.alignment <== alignment
    blockGrid.alignment <== alignment

    activeSuburb.onChange { (_, _, _) => updateBlockGridOffset() }

    suburbGrid.lastHoveredCell.onChange { (_, _, _) => update() }
    suburbGrid.selectedCell.onChange { (_, _, _) => update() }
    suburbGrid.defaultSource.onChange { (_, _, _) => update() }
    blockGrid.lastHoveredCell.onChange { (_, _, _) => update() }
    blockGrid.selectedCell.onChange { (_, _, _) => update() }
    blockGrid.defaultSource.onChange { (_, _, _) => update() }

    searchField.text.onChange { (_, _, newValue) => onSearch(newValue) }

    searchField.onAction = _ => {
      if(suburbGrid.singleMatch.isDefined) { // select if there's a single match!
        suburbGrid.selectCell(suburbGrid.singleMatch.get)
        suburbGrid.resetHighlights()
        blockGrid.resetHighlights()
        searchField.text = ""
      }
    }

    suburbGrid.defaultSource.value = session.attributes.suburbIndex()
    blockGrid.defaultSource.value = session.attributes.position

    children = List(searchField, searchStatusLabel, suburbLabel, suburbGrid, blockLabelVBox, blockGrid,
      coordinatesLabel, coordinatesDeltaLabel)
  }

  def onSearch(text: String): Unit = {
    // if search text isn't empty...
    val cleanString = text.trim.toLowerCase
      .replace(".", "")
      .replace(",", "")
    if(!cleanString.isEmpty) {
      val searchWords = cleanString.split(" ")

      // predicate construction!
      cleanString match {
        case MapBox.coordinatesRegex(xCoord, yCoord) =>
          val coordinatePredicate: Option[MapGridViewDataSource => Boolean] = Some( { dataSource =>
            val (x, y) = (xCoord.toInt, yCoord.toInt)
            val block = dataSource.asInstanceOf[Block]
            block.x == x && block.y == y
          })
          suburbGrid.resetHighlights()
          blockGrid.highlightPredicate.value = coordinatePredicate

        case _ =>
          val wordMatchPredicate: Option[MapGridViewDataSource => Boolean] = Some( { dataSource => {
            val cleanName = dataSource.getName.toLowerCase.replace(".", "")
            searchWords.foldLeft(true) { (doesMatch, currentWord) =>
              if (!doesMatch) doesMatch else cleanName.contains(currentWord)
            }
          }})
          suburbGrid.highlightPredicate.value = wordMatchPredicate
          blockGrid.highlightPredicate.value = wordMatchPredicate
      }

      // if there's only one match, make it the active suburb!
      if(suburbGrid.singleMatch.isDefined) {
        activeSuburb.value = suburbGrid.singleMatch.get
        // set its name as active
        suburbLabel.text = Suburb.suburbs(suburbGrid.singleMatch.get).name
      }
    }
    else {
      // remove the highlight predicate!
      suburbGrid.resetHighlights()
      blockGrid.resetHighlights()
      updateActiveSuburb()
    }
    update()
  }

  // -- accessors for location data from the session --

  def getSessionSuburb: String = {
    val suburbIndex = session.attributes.suburbIndex()
    if(suburbIndex.isDefined) Suburb.suburbs(suburbIndex.get).name
    else "Suburb Name"
  }

  def getSessionBlock: String = {
    if(session.attributes.position.isDefined) Block.blocks(session.attributes.position.get).name
    else "Block Name"
  }

  def getSessionCoordinates: String = {
    if(session.attributes.position.isDefined) {
      val coordinates = blockGrid.dataSourcesCoordinatesFromIndex(session.attributes.position.get)
      s"[${coordinates._1}, ${coordinates._2}]"
    }
    else "[x, y]"
  }

  def getSessionCoordinatesDelta: String = {
    if(session.attributes.position.isDefined && activeBlock.value.isDefined) {
      val x = activeBlock.value.get % blockGrid.dataSourceWidth - session.attributes.position.get % blockGrid.dataSourceWidth
      val y = activeBlock.value.get / blockGrid.dataSourceWidth - session.attributes.position.get / blockGrid.dataSourceWidth
      (x, y) match {
        case (0, 0) => "you are here"
        case _ =>
          s"${if(x != 0) s"${Math.abs(x)} ${if(x > 0) "east" else "west"}" else ""}" +
            s"${if(x != 0 && y != 0) " and " else ""}" +
            s"${if(y != 0) s"${Math.abs(y)} ${if(y > 0) "south" else "north"}" else ""}"
      }
    }
    else ""
  }

  // -- map datasource modifier --

  def updateBlockGridOffset(): Unit = {
    val (x, y) = MapGridView.cellCoordinatesFromIndex(activeSuburb.value)
    blockGrid.offsetX.value = x * 10
    blockGrid.offsetY.value = y * 10
  }

  // -- functions to update various UI elements --

  def updateActiveSuburb(): Unit = {
    activeSuburb.value = {
      if(suburbGrid.singleMatch.isDefined) suburbGrid.singleMatch.get
      else if(blockGrid.singleMatch.isDefined) Block.blocks(blockGrid.singleMatch.get).getSuburbIndex
      else if(suburbGrid.selectedCell.value.isDefined) suburbGrid.selectedCell.value.get
      else if(suburbGrid.lastHoveredCell.value.isDefined) suburbGrid.lastHoveredCell.value.get
      else if (session.attributes.suburbIndex().isDefined) session.attributes.suburbIndex().get
      else activeSuburb.value
    }
  }

  def updateActiveBlock(): Unit = {
    activeBlock.value = {
      if(blockGrid.singleMatch.isDefined)
        Some(blockGrid.singleMatch.get)
      else if(blockGrid.lastHoveredCell.value.isDefined)
        blockGrid.lastHoveredDataSourceIndex
      else if(blockGrid.selectedCell.value.isDefined)
        blockGrid.selectedDataSourceIndex
      else if (session.attributes.position.isDefined && activeSuburb.value == session.attributes.suburbIndex().get)
        Some(session.attributes.position.get)
      else None
    }

    // println(s"${if(activeBlock.value.isDefined) Block.blocks(activeBlock.value.get) else "no active block"}")
  }

  def updateSearchStatusLabel(): Unit = {
    searchStatusLabel.text = {
      if(suburbGrid.matchCount.isEmpty && blockGrid.matchCount.isEmpty) ""
      else s"${
        if(suburbGrid.matchCount.isDefined)
          s"${suburbGrid.matchCount.get} suburb${if (suburbGrid.matchCount.get != 1) "s" else ""}"
        else ""
      }${
        if(suburbGrid.matchCount.isDefined && blockGrid.matchCount.isDefined) ", " else ""
      }${
        if(blockGrid.matchCount.isDefined)
          s"${blockGrid.matchCount.get} block${if (blockGrid.matchCount.get != 1) "s" else ""}"
        else ""
      }"
    }
  }

  def updateSuburbLabel(): Unit = {
    suburbLabel.text = {
      if(suburbGrid.lastHoveredCell.value.isDefined) {
        suburbGrid.cells(suburbGrid.lastHoveredCell.value.get).dataSource.value.getName
      }
      else suburbGrid.dataSources(activeSuburb.value).getName
    }
  }

  def updateBlockLabel(): Unit = {
    blockText.text = {
      if(blockGrid.lastHoveredCell.value.isDefined) {
        blockGrid.cells(blockGrid.lastHoveredCell.value.get).dataSource.value.getName
      }

      else if(activeBlock.value.isDefined) blockGrid.dataSources(activeBlock.value.get).getName
      else "------"
    }
  }

  def updateCoordinatesLabel(): Unit = {
    val coordinates: Option[(Int, Int)] = {
      val block: Option[Block] = {
        if(activeBlock.value.isDefined) Some(blockGrid.dataSources(activeBlock.value.get).asInstanceOf[Block])
        else None
      }
      if(block.isDefined) Some((block.get.x, block.get.y))
      else None
    }

    coordinatesLabel.text = {
      if(coordinates.isDefined) s"[${coordinates.get._1}, ${coordinates.get._2}]"
      else "[--, --]"
    }
  }

  def updateCoordinatesDeltaLabel(): Unit = {
    coordinatesDeltaLabel.text = getSessionCoordinatesDelta
  }

  def update(): Unit = {
    suburbGrid.defaultSource.value = session.attributes.suburbIndex()
    blockGrid.defaultSource.value = session.attributes.position
    updateActiveSuburb()
    updateActiveBlock()
    updateSearchStatusLabel()
    updateSuburbLabel()
    updateBlockLabel()
    updateCoordinatesLabel()
    updateCoordinatesDeltaLabel()
  }

  init()
}
