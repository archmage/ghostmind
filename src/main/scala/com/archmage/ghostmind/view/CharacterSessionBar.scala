package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.CharacterSession
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label}
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
      case _: NullPointerException =>
        image = new Image(getClass.getResourceAsStream("assets/human-icon.png"))
    }
    fitWidth = 50
    fitHeight = 50
  }

  val nameplate = new CharacterNameplate()

  val hpBar = new GhostProgressBar {
    bar.id = "HpBar"
  }

  val apBar = new GhostProgressBar {
    bar.id = "ApBar"
  }

  val barBox = new VBox {
    alignment = Pos.Center
    spacing = 3
    children = List(hpBar, apBar)
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

  children = List(avatar, nameplate, barBox, xpLabel, growRegion, characterButton)

  def update(): Unit =  {
    if(session.attributes.isDefined) {
      val attributes = session.attributes.get
      hpBar.text.text = session.hpString()
      hpBar.bar.progress = attributes.hpDouble()
      apBar.text.text = session.apString()
      apBar.bar.progress = session.apDouble()
      xpLabel.text = s"${session.attributes.get.xp.toString}xp"
    }

    nameplate.session = Some(session)
    nameplate.update()
  }

  update()
}
