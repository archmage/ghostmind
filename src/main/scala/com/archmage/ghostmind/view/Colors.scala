package com.archmage.ghostmind.view

import scalafx.Includes._
import scalafx.geometry.Insets
import scalafx.scene.layout.{Background, BackgroundFill, CornerRadii}
import scalafx.scene.paint.Color

object Colors {

  val red = Color.apply(0.6, 0.1, 0.2, 0.7)
  val green = Color.apply(0.1, 0.6, 0.2, 0.7)

  val characterBoxNormal = Color.apply(0.22, 0.22, 0.22, 1)
  val characterBoxHighlight = Color.apply(0.3, 0.3, 0.3, 1)

  val normalBackground =
    new Background(Array(new BackgroundFill(Colors.characterBoxNormal, CornerRadii.Empty, Insets.Empty)))
  val hoverBackground =
    new Background(Array(new BackgroundFill(Colors.characterBoxHighlight, CornerRadii.Empty, Insets.Empty)))
}
