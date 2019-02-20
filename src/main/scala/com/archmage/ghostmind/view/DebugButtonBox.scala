package com.archmage.ghostmind.view

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label
import scalafx.scene.layout.FlowPane

class DebugButtonBox extends FlowPane {
  alignment = Pos.TopLeft
  padding = Insets(10)
  hgap = 5
  vgap = 5

  children = new Label {
    id = "WhiteText"
    text = "nothing here yet"
  }

}
