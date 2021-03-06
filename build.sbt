lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.archmage",
      scalaVersion := "2.12.7",
      version      := "0.1.0"
    )),
    name := "ghostmind",
    libraryDependencies ++= Seq (
      "org.scalafx" %% "scalafx" % "11-R16",
      "net.ruippeixotog" %% "scala-scraper" % "2.1.0",
      "org.json4s" %% "json4s-jackson" % "3.5.2",
      "org.scala-graph" %% "graph-core" % "1.12.5"
    )
  )
