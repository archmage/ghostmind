package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.{CharacterSession, UrbanDeadModel}
import javafx.event.{ActionEvent, EventHandler}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupDocument
import scalafx.application.Platform
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Button
import scalafx.scene.layout.FlowPane

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ActionButtonBox(session: CharacterSession) extends FlowPane {
  alignment = Pos.TopLeft
  padding = Insets(10)
  hgap = 5
  vgap = 5

  val speechClosure: EventHandler[ActionEvent] = _ => speech()

  val speechField = new GhostField {
    maxWidth = 140
    promptText = "say something"
    onAction = speechClosure
  }

  val speechSubmitButton = new Button {
    text = "speak your mind"
    onAction = speechClosure
  }

  def speech(): Unit = {
    val message = speechField.text.value.trim
    speechField.text.value = ""
    StatusBar.status = "raising our voice..."
    Future[Option[JsoupDocument]] {
      UrbanDeadModel.speakAction(message, session)
    } map { response =>
      UrbanDeadModel.parseMapCgi(response.get, session)
    } map { _ =>
      Platform.runLater(() => {
        UIModel.updateUI()
        StatusBar.status = "done speaking"
      })
    }
  }

  children = List(speechField, speechSubmitButton)
}
