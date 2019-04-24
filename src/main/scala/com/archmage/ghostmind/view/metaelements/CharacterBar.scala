package com.archmage.ghostmind.view.metaelements

import com.archmage.ghostmind.model.{CharacterSession, UrbanDeadModel}
import com.archmage.ghostmind.view._
import com.archmage.ghostmind.view.assets.AssetManager
import com.archmage.ghostmind.view.components.{CharacterNameplate, GhostProgressBar}
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
    val stream = AssetManager.getClass.getResourceAsStream(s"avatars/${session.username}.png")
    if(stream != null) image = new Image(stream)
    else image = AssetManager.humanImage

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
        UrbanDeadModel.processMapCgi(response.get, session)
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
    hpBar.text.text = session.attributes.hpString()
    hpBar.bar.progress = session.attributes.hpDouble()
    apBar.text.text = session.attributes.apString()
    apBar.bar.progress = session.attributes.apDouble()
    xpBar.text.text = session.attributes.xpStringLong()
    xpBar.bar.progress = session.attributes.xpDouble()
    hitsBar.text.text = session.attributes.hitsString()
    hitsBar.bar.progress = session.attributes.hitsDouble()
  }

  update()
}
