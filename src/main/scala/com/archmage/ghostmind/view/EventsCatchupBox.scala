package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.CharacterSession
import scalafx.geometry.Insets
import scalafx.scene.control.Label
import scalafx.scene.layout.{Priority, VBox}

class EventsCatchupBox(val session: CharacterSession) extends VBox {
  id = "SolidGreyBorder"
  padding = Insets(10)
  hgrow = Priority.Never

  val heading = new Label {
    id = "BoxHeading"
  }

  if(session.events.isEmpty) {
    heading.text = "no events since last turn"
    children = heading
  }
  else {
    heading.text = "since your last turn:"
    val events = session.events.get.map { event => new Label {
      id = "WhiteText"
      text = event.formatOutput()
      padding = Insets(0, 0, 0, 6)
      wrapText = true
    }}.toList
    children = List(heading, new GhostScrollPane {
      padding = Insets(5)
      vgrow = Priority.Always
      content = new VBox {
        children = events
        spacing = 10
        maxWidth = 400
      }
    })
  }
}
