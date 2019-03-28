package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.{CharacterSession, UrbanDeadModel}
import javafx.event.{ActionEvent, EventHandler}
import net.ruippeixotog.scalascraper.model.Document
import scalafx.application.Platform
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Button
import scalafx.scene.layout.{FlowPane, Priority}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ActionButtonBox(session: CharacterSession) extends FlowPane {
  alignment = Pos.TopLeft
  padding = Insets(10)
  hgap = 5
  vgap = 5
  hgrow = Priority.Always

  val speechClosure: EventHandler[ActionEvent] = _ => speech()
  val searchClosure: EventHandler[ActionEvent] = _ => search()

  val speechField = new GhostField {
    hgrow = Priority.Always
    prefWidth = 300
    promptText = "say something"
    onAction = speechClosure
  }

  val speechSubmitButton = new Button {
    text = "speak your mind"
    onAction = speechClosure
  }

  val searchButton = new Button {
    text = "search"
    onAction = searchClosure
  }

  // movement buttons


  def performAction(startMessage: String, endMessage: String, action: () => Option[Document]): Unit = {
    StatusBar.status = startMessage
    Future[Option[Document]] {
      action.apply()
    } map { response =>
      UrbanDeadModel.parseMapCgi(response.get, session)
    } map { _ =>
      Platform.runLater(() => {
        UIModel.updateUI()
        StatusBar.status = endMessage
      })
    }
  }

  def speech(): Unit = {
    val message = speechField.text.value.trim
    speechField.text.value = ""
    performAction("raising our voice...", "done speaking",
      () => UrbanDeadModel.speakAction(message, session))
  }

  def search(): Unit = {
    performAction("searching for an item...", "done searching",
      () => UrbanDeadModel.searchAction(session))
  }

  children = List(searchButton, speechField, speechSubmitButton)
}
