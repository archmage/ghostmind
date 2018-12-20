package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.CharacterSession
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label
import scalafx.scene.image.ImageView
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text._

object EventsCatchupBox {
  val textPadding = 6
}

class EventsCatchupBox(val session: CharacterSession) extends VBox {
  id = "SolidGreyBorder"
  padding = Insets(10)
  spacing = 10

  val heading = new Label {
    id = "BoxHeading"
  }

  if(session.events.isEmpty) {
    heading.text = "no events since last turn"
    children = heading
  }
  else {
    heading.text = "since your last turn:"
    val events = session.events.get.map { event => {
      val icon = new ImageView {
        image = event.eventType.image
        fitWidth = 26
        fitHeight = 26
        // TODO add some padding between image and text
      }

      val textFlow = new TextFlow {
        children = List(icon, new Text {
          fill = Color.White
          text = event.formatOutput()
        })

        padding = Insets(4, 0, 0, EventsCatchupBox.textPadding)
      }

      val eventBox = new HBox {
        alignment = Pos.CenterLeft
        children = List(icon, textFlow)
      }

      eventBox.maxWidth <== this.width - 34 // TODO make this less shit
      eventBox
    }}.toList

    // TODO figure out why this is behaving strangely with height sizing
    val scrollPane = new GhostScrollPane {
      content = new VBox {
        children = events
        spacing = 5
      }
    }

    // temporary workaround
    maxHeight = 300

    children = List(heading, scrollPane)
  }
}
