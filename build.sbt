import AssemblyKeys._

name := "multibot"

version := "1.0"

scalaVersion := "2.9.2"

libraryDependencies ++= Seq(
  "com.chuusai" %% "shapeless" % "1.2.2",
  "org.scalaz" %% "scalaz-core" % "7.0-SNAPSHOT",
  "org.scalaz" %% "scalaz-iteratee" % "7.0-SNAPSHOT",
  "org.scalaz" %% "scalaz-effect" % "7.0-SNAPSHOT",
  "org.scalaz" %% "scalaz-typelevel" % "7.0-SNAPSHOT",
  "net.databinder" %% "dispatch-http" % "0.8.8",
  "pircbot" % "pircbot" % "1.5.0",
  "org.scala-lang" % "scala-compiler" % "2.9.2",
  "org.json4s" %% "json4s-native" % "3.1.0-SNAPSHOT",
  "org.scalacheck" %% "scalacheck" % "1.10-SNAPSHOT",
  "org.jruby" % "jruby-complete" % "1.7.0"
)

// autoCompilerPlugins := true

assembleArtifact in packageBin := false

seq(assemblySettings: _*)

// mergeStrategy in assembly := (e => MergeStrategy.first)

resolvers += "Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
