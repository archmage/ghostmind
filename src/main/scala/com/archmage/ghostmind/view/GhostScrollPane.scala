package com.archmage.ghostmind.view

import scalafx.scene.control.ScrollPane
import scalafx.scene.control.ScrollPane.ScrollBarPolicy
import scalafx.scene.layout.Priority

class GhostScrollPane extends ScrollPane {
  hbarPolicy = ScrollBarPolicy.Never
  vbarPolicy = ScrollBarPolicy.Never

  hgrow = Priority.Never
}
