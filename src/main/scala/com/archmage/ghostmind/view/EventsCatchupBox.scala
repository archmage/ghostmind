package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.CharacterSession
import scalafx.geometry.Insets
import scalafx.scene.control.{Label, ScrollPane}
import scalafx.scene.layout.VBox

class EventsCatchupPane(val session: CharacterSession) extends ScrollPane {
  id = "root"
  content = new EventsCatchupBox(session)
  // change the scroll speed, somehow
}

class EventsCatchupBox(val session: CharacterSession) extends VBox {

  id = "SolidGreyBorder"
  padding = Insets(10)
  spacing = 10

  if(session.events.isEmpty) children = new Label {
    id = "BoxHeading"
    text = "no events since last turn"
  }
  else {
    val sinceYourLastTurn = new Label {
      id = "BoxHeading"
      text = "since your last turn:"
    }
    val events = session.events.get.map { event => new Label {
      id = "WhiteText"
      text = event.formatOutput()
      padding = Insets(0, 0, 0, 6)
      wrapText = true
    }}.toList
    children = sinceYourLastTurn :: events
  }
}
