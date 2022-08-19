name := "akka-praha-study"
version := "1.0"
scalaVersion := "2.13.8"
lazy val akkaHttpVersion = "10.2.9"
lazy val akkaVersion = "2.6.19"

fork := true // これは後々ちゃんと調べたい

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8" % Test,
  "org.iq80.leveldb" % "leveldb" % "0.9" % Test,
  "org.scalatest" %% "scalatest" % "3.1.0" % Test
)

// libraryDependencies ++= Seq(
//   "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
//   "ch.qos.logback" % "logback-classic" % "1.2.3",
//   "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
//   "org.scalatest" %% "scalatest" % "3.1.4" % Test
// )
