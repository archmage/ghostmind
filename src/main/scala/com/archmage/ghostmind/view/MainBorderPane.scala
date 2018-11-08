package com.archmage.ghostmind.view

import scalafx.geometry.Insets
import scalafx.scene.control.TabPane
import scalafx.scene.layout.BorderPane

class MainBorderPane extends BorderPane {

  val loginBar = new LoginBar
  val rightTabPane = new TabPane

  padding = Insets(10)

  rightTabPane.padding = Insets(10, 0, 0, 0)
  rightTabPane.tabs = Seq(new ContactsTab)

  top = loginBar
  right = rightTabPane
}

