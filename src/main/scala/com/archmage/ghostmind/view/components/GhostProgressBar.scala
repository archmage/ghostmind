package com.archmage.ghostmind.view.components

import scalafx.geometry.Pos
import scalafx.scene.control.{Label, ProgressBar}
import scalafx.scene.layout.StackPane

class GhostProgressBar extends StackPane {

  alignment = Pos.Center

  val bar = new ProgressBar()
  val text = new Label() {
    id = "ProgressBarText"
  }

  children = List(bar, text)
}
