package com.archmage.ghostmind.view.components

import com.archmage.ghostmind.view.assets.AssetManager
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label
import scalafx.scene.image.ImageView
import scalafx.scene.layout.StackPane

class MailIcon extends StackPane {
  alignment = Pos.TopLeft

  val icon = new ImageView {
    image = AssetManager.mailImage
    preserveRatio = true
    fitWidth = 30
  }

  val mailCount = new Label {
    padding = Insets(10, 0, 0, 16)
    id = "MailCount"
    text = "0"
  }

  children = List(icon, mailCount)
}
