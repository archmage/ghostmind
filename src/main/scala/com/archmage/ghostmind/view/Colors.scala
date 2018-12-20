package com.archmage.ghostmind.view

import scalafx.Includes._
import scalafx.geometry.Insets
import scalafx.scene.layout.{Background, BackgroundFill, CornerRadii}
import scalafx.scene.paint.Color

object Colors {

  val characterBoxNormal = Color(0.22, 0.22, 0.22, 1)
  val characterBoxHighlight = Color(0.3, 0.3, 0.3, 1)

  val normalBackground =
    new Background(Array(new BackgroundFill(Colors.characterBoxNormal, CornerRadii.Empty, Insets.Empty)))
  val hoverBackground =
    new Background(Array(new BackgroundFill(Colors.characterBoxHighlight, CornerRadii.Empty, Insets.Empty)))
}
