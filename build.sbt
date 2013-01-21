import AssemblyKeys._

name := "multibot"

version := "1.0"

scalaVersion := "2.10.0"

libraryDependencies ++= {
    val scalazVersion = "7.0-SNAPSHOT"
    val crossVersion = CrossVersion.fullMapped{case "2.10.0" => "2.10"}
    Seq(
        "com.chuusai" %% "shapeless" % "1.2.4-SNAPSHOT" cross CrossVersion.fullMapped{case "2.10.0" => "2.10.0-RC5"},
        "org.scalaz" %% "scalaz-core" % scalazVersion cross crossVersion,
        "org.scalaz" %% "scalaz-iteratee" % scalazVersion cross crossVersion,
        "org.scalaz" %% "scalaz-effect" % scalazVersion cross crossVersion,
        "org.scalaz" %% "scalaz-typelevel" % scalazVersion cross crossVersion,
        "pircbot" % "pircbot" % "1.5.0",
        "org.scala-lang" % "scala-compiler" % "2.10.0",
        "org.scalacheck" %% "scalacheck" % "1.10.1-SNAPSHOT" cross CrossVersion.full,
        "net.databinder" %% "dispatch-http" % "0.8.8",
        "org.json4s" %% "json4s-native" % "3.1.0",
        "org.jruby" % "jruby-complete" % "1.7.2"
)}


// autoCompilerPlugins := true

assembleArtifact in packageBin := false

seq(assemblySettings: _*)

scalacOptions ++= Seq("-feature", "-language:_", "-deprecation", "-Xexperimental")

// mergeStrategy in assembly := (e => MergeStrategy.first)

resolvers += "Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
