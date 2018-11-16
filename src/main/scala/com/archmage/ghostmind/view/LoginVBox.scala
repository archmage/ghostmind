package com.archmage.ghostmind.view

import javafx.event.{ActionEvent, EventHandler}
import scalafx.application.Platform
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label, PasswordField, ProgressIndicator}
import scalafx.scene.layout.VBox

class LoginVBox(onSubmit: (String, String) => Unit, completion: () => Unit) extends VBox {

  alignment = Pos.Center
  padding = Insets(10)
  spacing = 10

  val loginClosure: EventHandler[ActionEvent] = _ => login()
  val logoutClosure: EventHandler[ActionEvent] = _ => logout()

  val usernameField = new GhostField {
    maxWidth = 140
    promptText = "username"
    onAction = loginClosure
    alignment = Pos.Center
  }
  val passwordField = new PasswordField {
    maxWidth = 140
    promptText = "password"
    style = GhostField.showPromptTextStyle
    onAction = loginClosure
    alignment = Pos.Center
  }
  val loginLogoutButton = new Button {
    text = "login"
    onAction = loginClosure
  }
  val loggedInAsLabel = new Label {
    id = "WhiteText"
  }
  val indicator = new ProgressIndicator()

  children = List(usernameField, passwordField, loginLogoutButton)

  def login(): Unit = {
    val username = usernameField.text.value.trim
    val password = passwordField.text.value.trim

    if(username.isEmpty || password.isEmpty) return

    children = indicator

    new Thread(() => {
      onSubmit.apply(username, password)

      Platform.runLater {
        loggedInAsLabel.text = s"logged in as $username"
        loginLogoutButton.text = "logout"
        loginLogoutButton.onAction = logoutClosure
        children = List(loggedInAsLabel, loginLogoutButton)
        completion.apply()
      }
    }).start()
  }

  def logout(): Unit = {
    loginLogoutButton.text = "login"
    loginLogoutButton.onAction = loginClosure
    children = List(usernameField, passwordField, loginLogoutButton)
    usernameField.requestFocus()
    usernameField.selectEnd()
    usernameField.deselect()
  }
}
