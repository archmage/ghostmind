package com.archmage.ghostmind.model

sealed abstract class LoginOutcome

case object Success extends LoginOutcome
case object AlreadyLoggedIn extends LoginOutcome
case object ServerInaccessible extends LoginOutcome
case object BadCredentials extends LoginOutcome