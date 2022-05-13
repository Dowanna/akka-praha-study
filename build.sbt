name := "akka-praha-study"
version := "1.0"
scalaVersion := "2.13.8"
lazy val akkaVersion = "2.6.19"

fork := true // これは後々ちゃんと調べたい

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.scalatest" %% "scalatest" % "3.1.0" % Test
)
