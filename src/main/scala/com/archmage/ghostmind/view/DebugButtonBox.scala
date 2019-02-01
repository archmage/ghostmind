package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.UrbanDeadModel
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Button
import scalafx.scene.layout.FlowPane

class DebugButtonBox extends FlowPane {
  alignment = Pos.TopLeft
  hgap = 5
  vgap = 5

  val reloadUIButton = new Button {
    id = "rich-blue"
    text = "reload UI"
  }

  val reviveButton = new Button {
    id = "rich-blue"
    text = "revive"
    onAction = _ => UrbanDeadModel.tryAndRevive()
  }

  children = List(reloadUIButton, reviveButton)

}
