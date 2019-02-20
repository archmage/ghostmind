package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.CharacterSession
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label
import scalafx.scene.image.ImageView
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.text._

object EventsCatchupBox {
  val textPadding = 6
}

class EventsCatchupBox(val session: CharacterSession) extends VBox with Updateable {
  spacing = 10
  var compact = false

  val heading = new Label {
    id = "BoxHeading"
    visible = !compact
  }

  def update(): Unit = {
    if(session.events.isEmpty) {
      heading.text = "no events since last turn"
      if(!compact) children = heading
    }
    else {
      heading.text = "since your last turn:"
      val events = session.events.get.reverse.map { event => {
        val icon = new ImageView {
          image = event.eventType.image
          fitWidth = 26
          fitHeight = 26
          // TODO add some padding between image and text
        }

        val textFlow = new TextFlow {
          children = event.textElements()
          padding = Insets(4, 0, 0, EventsCatchupBox.textPadding)
          visible = !compact
        }

        val eventBox = new HBox {
          alignment = Pos.CenterLeft
          children = if(compact) List(icon) else List(icon, textFlow)
        }

        eventBox.maxWidth <== this.width - 40 // TODO make this less shit
        eventBox
      }}.toList

      // TODO figure out why this is behaving strangely with height sizing
      val scrollPane = new GhostScrollPane {
        minWidth = if(compact) 60 else 300
        content = new VBox {
          spacing = 5
          children = events
        }
      }

      // temporary workaround
  //    maxHeight = 300

      children = if(compact) List(scrollPane) else List(heading, scrollPane)
    }
  }

  update()
}
