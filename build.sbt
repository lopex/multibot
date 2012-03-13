import AssemblyKeys._

name := "multibot"

version := "1.0"

scalaVersion := "2.9.1"

libraryDependencies ++= Seq(
  "com.chuusai" %% "shapeless" % "1.1.0",
  "org.scalaz" %% "scalaz-core" % "7.0-SNAPSHOT",
  "org.scalaz" %% "scalaz-iteratee" % "7.0-SNAPSHOT",
  "org.scalaz" %% "scalaz-effect" % "7.0-SNAPSHOT",
  "org.scalaz" %% "scalaz-typelevel" % "7.0-SNAPSHOT",
  "net.databinder" %% "dispatch-http" % "0.8.5",
  "pircbot" % "pircbot" % "1.4.2",
  "org.scala-lang" % "scala-compiler" % "2.9.1",
  "net.liftweb" %% "lift-json" % "2.4",
  "org.scala-tools.testing" %% "scalacheck" % "1.9"
)

// autoCompilerPlugins := true

assembleArtifact in packageBin := false

seq(assemblySettings: _*)

resolvers += ScalaToolsSnapshots

// resolvers += Classpaths.typesafeResolver
