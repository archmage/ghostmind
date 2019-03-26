package com.archmage.ghostmind

import scala.language.postfixOps
import scalafx.Includes._
import scalafx.animation.{Interpolator, Timeline}
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle

object ScalaFXAnimation extends JFXApp {
  val rect1 = new Rectangle {
    width = 20
    height = 480
    x = -22
    fill = Color.Red
  }
  val rect2 = new Rectangle {
    width = 50
    height = 20
    y = -50
    fill = Color.LightGreen
  }
  val timeline1 = new Timeline {
    cycleCount = Timeline.Indefinite
    autoReverse = true
    keyFrames = Seq(
      at(6 s) {rect1.x -> 642 tween Interpolator.EaseBoth},
    )
  }

  val timeline2 = new Timeline {
    cycleCount = Timeline.Indefinite
    keyFrames = Seq(
      at(6 s) {rect2.y -> 700},
      at(4.2 s) {rect2.height -> 40},
      at(4.2 s) {rect2.width -> 500},
      at(5 s) {rect2.rotate -> 720}
    )
  }
  timeline1.play()
  timeline2.play()
  stage = new PrimaryStage {
    width = 640
    height = 480
    scene = new Scene {
      content = List(rect1, rect2)
    }
  }
}
