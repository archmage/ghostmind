package com.archmage.ghostmind.view

import com.archmage.ghostmind.model._
import com.archmage.ghostmind.view.assets.AssetManager
import javafx.scene.input.MouseButton
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout._

class CharacterBox(var session: Option[CharacterSession] = None, val index: Int) extends VBox with Updateable {

  alignment = Pos.Center
  prefWidth = 180
  prefHeight = 252

  var deleteConfirm = false

  onMouseExited = _ => {
    if(deleteConfirm) {
      deleteConfirm = false
      update()
    }
  }

  // session absent
  val plusIcon = new ImageView{
    image = AssetManager.plusImage
    fitWidth = 50
    fitHeight = 50
  }
  val addCharacterLabel = new Label {
    id = "Subtitle"
    text = "add a character"
    margin = Insets(8, 0, 0, 0)
  }
  var loginBox = new LoginVBox(login, loginComplete)

  val avatar = new ImageView {
    fitWidth = 90
    fitHeight = 90
    margin = Insets(5, 0, 0, 0)
  }
  val mailIcon = new MailIcon {
    margin = Insets(8, 0, 0, 8)
  }
  val deleteButton = new Button {
    id = "RedXButton"
    text = "X"
    onMouseReleased = _ => {
      if(deleteConfirm) {
        UrbanDeadModel.sessions(index) = None
        UrbanDeadModel.saveCharacters()

        avatar.image.value = null
        session = None
        loginBox = new LoginVBox(login, loginComplete)

        deleteConfirm = false
      }
      else {
        deleteConfirm = true
      }
      update()
    }
  }
  val topStackPane = new StackPane {
    alignment = Pos.TopCenter
    children = List(
      avatar, new HBox {
        alignment = Pos.TopCenter
        children = List(mailIcon, new Region { hgrow = Priority.Always }, deleteButton)
      }
    )
  }

  val groupLabel = new Label {
    id = "Subtitle"
  }

  val nameplate = new CharacterNameplate()

  val status = new Label {
    id = "CharacterBoxStatusText"
    margin = Insets(8)
  }

  val hpBar = new GhostProgressBar {
    bar.id = "HpBar"
    bar.prefWidth = Integer.MAX_VALUE
  }

  val apBar = new GhostProgressBar {
    bar.id = "ApBar"
    bar.prefWidth = Integer.MAX_VALUE
  }

  val hitsBar = new GhostProgressBar {
    bar.id = "HitsBar"
    bar.prefWidth = Integer.MAX_VALUE
  }

  def update(): Unit = {
    Platform.runLater(() => {
      if(session.isEmpty) {
        id = "DottedGreyBorder"
        children = List(plusIcon, addCharacterLabel)
        onMouseClicked = event => {
          if(event.getButton == MouseButton.PRIMARY) children = loginBox
        }
      }

      else {
        val session = this.session.get

        if (avatar.image.value == null) {
          try {
            avatar.image = new Image(getClass.getResourceAsStream(s"assets/${session.username}.png"))
          }
          catch {
            case _: Exception =>
              avatar.image = AssetManager.humanImage
          }
        }

        val groupString = if(session.attributes.isEmpty) "[unknown group]"
                          else s"[${
          if(session.attributes.get.group == "none") "no group" else session.attributes.get.group
        }]"
        if(deleteConfirm) {
          id = "RedWarnBox"
          groupLabel.id = "RedWarnText"
          groupLabel.text = "click again to delete"
        }
        else {
          id = "SolidGreyBorder"
          groupLabel.id = "Subtitle"
          groupLabel.text = groupString
        }
        status.text = session.state.value.toString.dropRight(2).toUpperCase
        session.state.value match {
          case Offline() => status.style = "-fx-text-fill: #ff0000;"
          case Connecting() => status.style = "-fx-text-fill: #ffff00;"
          case Online() => status.style = "-fx-text-fill: #00ff00;"
        }
        mailIcon.visible = session.newEvents > 0
        if(session.newEvents > 0) mailIcon.mailCount.text = session.newEvents.toString
        children = List(topStackPane, nameplate, groupLabel, status, hpBar, apBar, hitsBar)

        hpBar.text.text = session.hpString()
        hpBar.bar.progress = session.hpDouble()
        apBar.text.text = session.apString()
        apBar.bar.progress = session.apDouble()
        hitsBar.text.text = session.hitsString()
        hitsBar.bar.progress = session.hitsDouble()

        onMouseClicked = event => {
          if(event.getButton == MouseButton.PRIMARY) session.state.value match {
            case Offline() =>
              new Thread(() => login(session.username, session.password)).start()
            case Connecting() => ()
            case Online() => startSession()
          }
          else if(event.getButton == MouseButton.SECONDARY) {
            if(session.state.value == Online()) logout()
          }
        }
      }
      if(deleteConfirm) background.unbind()
      else background <== when(hover) choose Colors.hoverBackground otherwise Colors.normalBackground

      nameplate.session = session
      nameplate.update()
    })
  }

  def addOnSessionStateChangeUpdate(): Unit = {
    if(session.isDefined) session.get.state.onChange  { (_, _, _) => update() }
  }

  def login(username: String, password: String): Unit = {
    if(session.isEmpty) {
      session = Some(CharacterSession(username, password))
      addOnSessionStateChangeUpdate()
      update()
    }
    val result = UrbanDeadModel.loginExistingSession(session.get, index)
    if(!result) {
      Platform.runLater(() => children = loginBox)
    }
  }

  def loginComplete(): Unit = {
    session.get.state.value = Online()
  }

  def startSession(): Unit = {
    UrbanDeadModel.activeSession = session
    UIModel.state = Main()
  }

  def logout(): Unit = {
    session.get.resetBrowser()
    StatusBar.status = s"""logged out as "${session.get.username}""""
    session.get.state.value = Offline()
  }

  addOnSessionStateChangeUpdate()
  update()
}
