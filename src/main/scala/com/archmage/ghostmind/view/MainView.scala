package com.archmage.ghostmind.view

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.Includes._
import scalafx.scene.image.Image


object MainView extends JFXApp {

  // views
  val mainBorderPane = new MainBorderPane

  // setup
  val titleText = s"ghostmind"

  // finalisation
  stage = new PrimaryStage {
    title = titleText
    icons +=
    new Image(getClass.getResourceAsStream("assets/ghost.png"))
    width = 800
    height = 600
    scene = new Scene {
      stylesheets += this.getClass.getResource("assets/style.css").toExternalForm
      root = mainBorderPane
    }
  }
}
