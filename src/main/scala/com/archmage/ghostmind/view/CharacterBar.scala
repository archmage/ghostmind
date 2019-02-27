package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.{CharacterSession, UrbanDeadModel}
import net.ruippeixotog.scalascraper.model.Document
import scalafx.application.Platform
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Button
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{HBox, Priority, Region, VBox}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CharacterBar(val session: CharacterSession) extends HBox with Updateable {

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

  val barBox1 = new VBox {
    alignment = Pos.Center
    spacing = 3
    children = List(hpBar, apBar)
  }

  val hitsBar = new GhostProgressBar {
    bar.id = "HitsBar"
  }

  val xpBar = new GhostProgressBar {
    bar.id = "XpBar"
    text.text = "0xp"
  }

  val barBox2 = new VBox {
    alignment = Pos.Center
    spacing = 3
    children = List(hitsBar, xpBar)
  }

  val growRegion = new Region {
    hgrow = Priority.Always
  }

  val refreshButton = new Button {
    text = "refresh"
    onAction = _ => {
      StatusBar.status = "refreshing..."
      Future[Option[Document]] {
        UrbanDeadModel.pollMapCgi(session)
      } map { response =>
        UrbanDeadModel.parseMapCgi(response.get, session)
      } map { _ =>
        Platform.runLater(() => {
          UIModel.updateUI()
          StatusBar.status = "done refreshing"
        })
      }
    }
  }

  val characterButton = new Button {
    text = "characters"
    onMouseReleased = _ => UIModel.state = Characters()
  }

  children = List(avatar, nameplate, barBox1, barBox2, growRegion, refreshButton, characterButton)

  def update(): Unit =  {
    nameplate.session = Some(session)
    nameplate.update()
    hpBar.text.text = session.hpString()
    hpBar.bar.progress = session.hpDouble()
    apBar.text.text = session.apString()
    apBar.bar.progress = session.apDouble()
    xpBar.text.text = session.xpStringLong()
    xpBar.bar.progress = Math.min(1, session.attributes.get.xp / 100.0)
    hitsBar.text.text = session.hitsString()
    hitsBar.bar.progress = session.hitsDouble()
  }

  update()
}
