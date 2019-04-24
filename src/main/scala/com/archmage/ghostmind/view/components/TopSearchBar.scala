package com.archmage.ghostmind.view.components

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, TextField}
import scalafx.scene.layout.{ColumnConstraints, GridPane, HBox, Priority}

class TopSearchBar extends GridPane {

  val col1 = new ColumnConstraints()
  val col2 = new ColumnConstraints()
  col1.setPercentWidth(20)
  col2.setPercentWidth(60)

  columnConstraints.addAll(col1, col2, col1)

  padding = Insets(2)
  alignment = Pos.Center

  val searchBar = new TextField {
    promptText = "Enter a search"
    hgrow = Priority.Always
  }

  val submitButton = new Button {
    text = "Submit"
  }

  val backButton = new Button {
    text = "< Back"
  }

  val searchElements = new HBox {
    children.addAll(searchBar, submitButton)
  }

  add(backButton, 0, 0)
  add(searchElements, 1, 0)
}
