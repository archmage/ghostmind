package com.archmage.ghostmind.view

import scalafx.scene.control.TextField

object GhostField {
  val showPromptTextStyle = "-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%);"
}

class GhostField extends TextField {

  style = GhostField.showPromptTextStyle

}
