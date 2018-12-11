package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.{Block, CharacterSession, Suburb}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox

object MapBox {
  val defaultBlockLabelText = "Block Name"
}

class MapBox(val session: CharacterSession) extends VBox {

  alignment = Pos.TopCenter
  padding = Insets(10)
  spacing = 10

  var suburbMatchCount: Option[Int] = None
  var lastSuburbMatch: Option[(MapGridRectangle, Int)] = None

  // ui stuff

  val searchField = new GhostField {
    promptText = "search"
  }
  val blockLabel = new Label {
    id = "WhiteText"
    text = MapBox.defaultBlockLabelText
  }
  val blockGrid: MapGridView = new MapGridView(Block.blocks, 100, (_, _) => (), () => ())
  val suburbLabel = new Label {
    id = "BoxHeading"
    text = defaultSuburbLabelText
  }
  val suburbGrid: MapGridView = new MapGridView(Suburb.suburbs, 10, (x, y) => {
    if(suburbGrid.selectedCell.value.isEmpty) {
      blockGrid.offsetX.value = x * 10
      blockGrid.offsetY.value = y * 10
    }
  }, () => ())

  suburbGrid.alignment <== alignment
  blockGrid.alignment <== alignment

  suburbGrid.lastHoveredCell.onChange { (_, _, _) => update() }
  suburbGrid.selectedCell.onChange { (_, _, _) => update() }
  blockGrid.lastHoveredCell.onChange { (_, _, _) => update() }
  blockGrid.selectedCell.onChange { (_, _, _) => update() }

  def defaultSuburbLabelText: String = session.username

  def getSearchResult(): Option[String] = {
    if(suburbMatchCount.isDefined) {
      if(suburbMatchCount.get == 1 && lastSuburbMatch.isDefined) {
        Some(lastSuburbMatch.get._1.dataSource.value.getName)
      }
      else Some(s"${if(suburbMatchCount.get <= 0) "no" else suburbMatchCount.get} matches${
        if(suburbMatchCount.get > 0) " found" else ""}")
    }
    else None
  }

  def update(): Unit = {
    suburbLabel.text = {
      // hovered cell
      if(suburbGrid.lastHoveredCell.value.isDefined) {
        suburbGrid.lastHoveredCell.value.get.dataSource.value.getName
      }
      else {
        // search results
        val searchResultString = getSearchResult()
        if(searchResultString.isDefined) searchResultString.get
        else {
          // selected cell
          val activeName = suburbGrid.getActiveName()
          if(activeName.isDefined) activeName.get
          // TODO session's current suburb
          else defaultSuburbLabelText
        }
      }
    }

    val blockText: Option[String] = blockGrid.getActiveName()

    blockLabel.text = if(blockText.isDefined) blockText.get else MapBox.defaultBlockLabelText

    if(suburbMatchCount.isDefined && suburbMatchCount.get == 1 && lastSuburbMatch.isDefined) {
      blockGrid.offsetX.value = 10 * (lastSuburbMatch.get._2 % suburbGrid.dataSourceWidth)
      blockGrid.offsetY.value = 10 * (lastSuburbMatch.get._2 / suburbGrid.dataSourceWidth)
    }
  }

  searchField.text.onChange { (_, _, newValue) => {
    suburbMatchCount = None
    if(lastSuburbMatch.isDefined) lastSuburbMatch.get._1.highlighted.value = false
    lastSuburbMatch = None

    if(!newValue.trim.isEmpty) {
      suburbMatchCount = Some(0)
      suburbGrid.cells.zipWithIndex.foreach { cell =>
        val doesMatch = cell._1.dataSource.value.getName.toLowerCase.contains(newValue.trim.toLowerCase)
        cell._1.highlighted.value = doesMatch
        if(doesMatch) {
          suburbMatchCount = Some(suburbMatchCount.get + 1)
          lastSuburbMatch = Some(cell)
        }
      }
      // block search?
    }
    else suburbGrid.cells.foreach { cell => cell.highlighted.value = false }
    update()
  }}

  searchField.onAction = _ => {
    if(suburbMatchCount.isDefined && suburbMatchCount.get == 1 && lastSuburbMatch.isDefined) {
      lastSuburbMatch.get._1.highlighted.value = false
      suburbGrid.selectCell(lastSuburbMatch.get._1)
      searchField.text = ""
    }
  }

  children = List(searchField, suburbLabel, suburbGrid, blockLabel, blockGrid)
}
