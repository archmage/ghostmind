package com.archmage.ghostmind.model

sealed abstract class ConnectivityState(val style: String)

case object Offline extends ConnectivityState("-fx-text-fill: #ff0000;")
case object Connecting extends ConnectivityState("-fx-text-fill: #ffff00;")
case object Retrieving extends ConnectivityState("-fx-text-fill: #ffff00;")
case object Online extends ConnectivityState("-fx-text-fill: #00ff00;")
