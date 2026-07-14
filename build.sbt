val scala3Version = "3.3.5"

assembly / assemblyOutputPath := file("./ttr-scala.jar")

enablePlugins(ScalafmtPlugin)
scalafmtOnCompile := true

enablePlugins(ScoverageSbtPlugin)
assembly / coverageEnabled := false
test / coverageEnabled := true

lazy val root = project
  .in(file("."))
  .settings(
    name := "ASMD-ai-ttr-scala",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,
    javacOptions ++= Seq("-source", "17.0", "-target", "17.0"),

    libraryDependencies ++= Seq(
        "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
        "com.github.vlsi.mxgraph" % "jgraphx" % "4.2.2",
        "com.lihaoyi" %% "upickle" % "4.4.3",
        "it.unibo.alice.tuprolog" % "2p-core" % "4.1.1",
        "it.unibo.alice.tuprolog" % "2p-ui" % "4.1.1",
        "dev.langchain4j" % "langchain4j" % "1.17.2",
        "dev.langchain4j" % "langchain4j-ollama" % "1.17.2",
        "dev.langchain4j" % "langchain4j-google-ai-gemini" % "1.17.2",
        "dev.langchain4j" % "langchain4j-open-ai" % "1.17.2",
        "org.scalatest" %% "scalatest" % "3.2.20" % Test,
        "org.scalatestplus" %% "mockito-5-23" % "3.2.20.0" % "test"
    )
  )
