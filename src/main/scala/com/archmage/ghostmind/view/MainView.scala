package com.archmage.ghostmind.view

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene

object MainView extends JFXApp {

  // views
  val mainBorderPane = new MainBorderPane

  // setup
  val titleText = s"ghostmind"

  // finalisation
  stage = new PrimaryStage {
    title = titleText
    width = 800
    height = 600
    scene = new Scene {
      root = mainBorderPane
    }
  }
}
