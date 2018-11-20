package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.CharacterSession
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label, ProgressBar}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{HBox, Priority, Region}

class CharacterSessionBar(val session: CharacterSession) extends HBox {

  alignment = Pos.CenterLeft
  spacing = 10
  padding = Insets(5)

  val avatar = new ImageView {
    try {
      image = new Image(getClass.getResourceAsStream(s"assets/${session.username}.png"))
    }
    catch {
      case npe: NullPointerException =>
        image = new Image(getClass.getResourceAsStream("assets/human-icon.png"))
    }
    fitWidth = 50
    fitHeight = 50
  }

  val nameLabel = new Label {
    id = "Title"
    text = session.username
  }

  val levelLabel = new Label {
    id = "WhiteText"
    text = s"Lv ???"
  }

  // eventually a bar or something
  val hpLabel = new Label {
    id = "WhiteText"
    text = s"HP 50/50" // implement later
  }

  val hpBar = new ProgressBar {
    progress = 1.0
  }

  val growRegion = new Region {
    hgrow = Priority.Always
  }

  val characterButton = new Button {
    text = "Characters"
    onMouseReleased = _ => UIModel.state = Characters()
  }

  children = List(avatar, nameLabel, levelLabel, hpLabel, hpBar, growRegion, characterButton)
}
