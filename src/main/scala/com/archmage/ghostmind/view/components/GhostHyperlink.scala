package com.archmage.ghostmind.view.components

import javafx.scene.input.MouseButton
import scalafx.scene.paint.Color
import scalafx.scene.text.Text

/**
  * a faux-hyperlink that will wrap nicely in TextFlow parents
  */
class GhostHyperlink extends Text {

  onMouseEntered = _ => style = "-fx-underline: true;"
  onMouseExited = _ => style = ""

  var onAction: Option[() => Unit] = None
  onMouseClicked = event => {
    if(!disabled.value && event.getButton == MouseButton.PRIMARY && onAction.isDefined) onAction.get.apply()
  }

  fill = Color.White
  disabled.onChange { (_, _, newValue) => fill = if(newValue) Color.Gray else Color.White }
}
