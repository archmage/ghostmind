package com.archmage.ghostmind.view.components

import com.archmage.ghostmind.model._
import com.archmage.ghostmind.view.assets.AssetManager
import com.archmage.ghostmind.view._
import javafx.scene.input.MouseButton
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout._

class CharacterBox(var session: Option[CharacterSession] = None, val index: Int) extends VBox with Updateable {

  alignment = Pos.Center
  prefWidth = 190
//  prefHeight = 320
  spacing = 8

  var deleteConfirm = false
  var addCharacterClicked = false

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

  val addCharacterBox = new VBox {
    alignment = Pos.Center
    children = List(plusIcon, addCharacterLabel)
  }

  var loginBox = new LoginVBox(login, onComplete)

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
        loginBox = new LoginVBox(login, onComplete)

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

  val nameplate = new CharacterNameplate {
    padding = Insets(0)
  }

  val groupLabel = new Label {
    id = "GroupText"
  }

  val suburbLabel = new Label {
    id = "Subtitle"
  }

  val blockLabel = new Label {
    id = "Subtitle"
  }

  val coordinatesLabel = new Label {
    id = "Subtitle"
  }

  val positionBox = new VBox {
    alignment = Pos.Center
    maxWidth = 170
    prefHeight = 50
    children = List(suburbLabel, blockLabel, coordinatesLabel)
  }

  val status = new Label {
    id = "ConnectivityStatusText"
  }

  val hpBar = new GhostProgressBar {
    bar.id = "HpBar"
    bar.prefWidth = Integer.MAX_VALUE
  }

  val apBar = new GhostProgressBar {
    bar.id = "ApBar"
    bar.prefWidth = Integer.MAX_VALUE
    bar
  }

  val hitsBar = new GhostProgressBar {
    bar.id = "HitsBar"
    bar.prefWidth = Integer.MAX_VALUE
  }

  val barBox = new VBox(hpBar, apBar, hitsBar)

  def update(): Unit = {
    Platform.runLater(() => {
      if(session.isEmpty) {
        id = "DottedGreyBorder"
        if(addCharacterClicked) children = loginBox
        else children = addCharacterBox
        onMouseClicked = event => {
          if(event.getButton == MouseButton.PRIMARY) addCharacterClicked = true
          else if(event.getButton == MouseButton.SECONDARY) {
            addCharacterClicked = false
            loginBox.usernameField.text = ""
            loginBox.passwordField.text = ""
          }
          if(addCharacterClicked) children = loginBox
          else children = addCharacterBox
        }
      }

      else {
        val session = this.session.get

        if (avatar.image.value == null) {
          val stream = AssetManager.getClass.getResourceAsStream(s"avatars/${session.username}.png")
          if(stream != null) avatar.image = new Image(stream)
          else avatar.image = AssetManager.humanImage
        }

        val groupString = if(session.attributes.group.isEmpty) "(unknown group)"
                          else s"${
          if(session.attributes.group.get == "none") "(no group)" else session.attributes.group.get
        }"
        if(deleteConfirm) {
          id = "RedWarnBox"
          groupLabel.id = "RedWarnText"
          groupLabel.text = "click again to delete"
        }
        else {
          // this is awful, but whatever
          id = session.state.value match {
            case Connecting => id.value
            case _ => "SolidGreyBorder"
          }
          groupLabel.id = "GroupText"
          groupLabel.text = groupString
        }

        suburbLabel.text = session.attributes.suburbName()
        blockLabel.text = session.attributes.blockName()
        coordinatesLabel.text = session.attributes.coordinatesString()

        status.text = session.state.value.toString.toUpperCase
        status.style = session.state.value.style

        mailIcon.visible = session.newEvents > 0
        if(session.newEvents > 0) mailIcon.mailCount.text = session.newEvents.toString

        // to allow the login box to take some time
        if(session.state.value != Connecting) {
          children = List(topStackPane, nameplate, groupLabel, positionBox, status, barBox)
        }

        hpBar.text.text = session.attributes.hpString()
        hpBar.bar.progress = session.attributes.hpDouble()
        apBar.text.text = session.attributes.apString()
        apBar.bar.progress = session.attributes.apDouble()
        hitsBar.text.text = session.attributes.hitsString()
        hitsBar.bar.progress = session.attributes.hitsDouble()

        onMouseClicked = event => {
          if(event.getButton == MouseButton.PRIMARY) session.state.value match {
            case Offline =>
              new Thread(() => login(session.username, session.password)).start()
            case Connecting => ()
            case Retrieving => ()
            case Online => startSession()
          }
          else if(event.getButton == MouseButton.SECONDARY) {
            if(session.state.value == Online) logout()
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
    if(session.isDefined) session.get.state.onChange { (_, _, _) => Platform.runLater(() => update()) }
  }

  def login(username: String, password: String): LoginOutcome = {
    if(session.isEmpty) {
      session = Some(CharacterSession(username, password))
      addOnSessionStateChangeUpdate()
      update()
    }
    UrbanDeadModel.loginExistingSession(session.get, index)
  }

  def onComplete(outcome: LoginOutcome): Unit = {
    outcome match {
      case Success =>
        loginBox.loginSuccess()
      case AlreadyLoggedIn => ()
      case ServerInaccessible =>
        session.get.state.value = Offline
        loginBox.loginFailure()
      case BadCredentials =>
        session = None
        avatar.image = null
        loginBox.loginFailure()
        Platform.runLater(() => children = loginBox)
    }
  }

  def startSession(): Unit = {
    UrbanDeadModel.activeSession = session
    UIModel.state = Main()
  }

  def logout(): Unit = {
    session.get.resetBrowser()
    StatusBar.status = s"""logged out as "${session.get.username}""""
    session.get.state.value = Offline
  }

  addOnSessionStateChangeUpdate()
  update()
}
