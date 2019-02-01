package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.CharacterSession
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{HBox, Priority, Region, VBox}

class CharacterBar(val session: CharacterSession) extends HBox {

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
    id = "rich-blue"
    text = "characters"
    onMouseReleased = _ => UIModel.state = Characters()
  }

  children = List(avatar, nameplate, barBox, xpLabel, growRegion, characterButton)

  def update(): Unit =  {
    hpBar.text.text = session.hpString()
    hpBar.bar.progress = session.hpDouble()
    apBar.text.text = session.apString()
    apBar.bar.progress = session.apDouble()
    xpLabel.text = s"${if(session.attributes.isDefined) session.attributes.get.xp.toString else 0}xp"

    nameplate.session = Some(session)
    nameplate.update()
  }

  update()
}
