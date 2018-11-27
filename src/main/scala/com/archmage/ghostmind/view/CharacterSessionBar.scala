package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.CharacterSession
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label, ProgressBar}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{HBox, Priority, Region, VBox}

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

  val nameplate = new CharacterNameplate()

  val hpLabel = new Label {
    id = "WhiteText"
    text = "HP 50/50"
    padding = Insets(0, -7, 0, 0)
  }

  val hpBar = new ProgressBar {
    progress = 1.0
  }

  val apLabel = new Label {
    id = "WhiteText"
    text = "AP 50/50"
    padding = Insets(0, -7, 0, 0)
  }

  val apBar = new ProgressBar {
    progress = 1.0
  }

  val xpLabel = new Label {
    id = "WhiteText"
    text = "0xp"
  }

  val growRegion = new Region {
    hgrow = Priority.Always
  }

  val characterButton = new Button {
    text = "Characters"
    onMouseReleased = _ => UIModel.state = Characters()
  }

  children = List(avatar, nameplate, xpLabel, hpLabel, hpBar, apLabel, apBar, growRegion, characterButton)

  def update(): Unit =  {
    if(session.attributes.isDefined) {
      hpLabel.text = s"HP ${session.attributes.get.hp.toString}/60"
      hpBar.progress = session.attributes.get.hp / 60.0
      apLabel.text = s"AP ${session.attributes.get.ap.toString}/50"
      apBar.progress = session.attributes.get.ap / 50.0
      xpLabel.text = s"${session.attributes.get.xp.toString}xp"
    }

    nameplate.session = Some(session)
    nameplate.update()
  }

  update()
}
