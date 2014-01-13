import AssemblyKeys._

name := "multibot"

version := "1.0"

scalaVersion := "2.10.3"

libraryDependencies ++= {
    val scalazVersion = "7.1.0-SNAPSHOT"
    val scalazContribVersion = "0.1.4"
    val crossVersion = CrossVersion.fullMapped{case "2.10.3" => "2.10"}
    val xVersion = CrossVersion.fullMapped{case "2.10.3" => "2.10.0"}
    val shapeCrossVersion = CrossVersion.fullMapped{case "2.10.3" => "2.10.3"}
    val scheckCrossVersion = CrossVersion.fullMapped{case "2.10.3" => "2.10.0"}
    Seq(
        "com.chuusai" %% "shapeless" % "2.0.0-SNAPSHOT" cross shapeCrossVersion,
        "org.scalaz" %% "scalaz-core" % scalazVersion cross crossVersion,
        "org.scalaz" %% "scalaz-iteratee" % scalazVersion cross crossVersion,
        "org.scalaz" %% "scalaz-effect" % scalazVersion cross crossVersion,
        "org.scalaz" %% "scalaz-typelevel" % scalazVersion cross crossVersion,
        "org.scalaz" %% "scalaz-scalacheck-binding" % scalazVersion cross crossVersion,
        "org.typelevel" %% "scalaz-contrib-210" % scalazContribVersion cross crossVersion,
        "org.scalaz" %% "scalaz-concurrent" % scalazVersion cross crossVersion,
        "pircbot" % "pircbot" % "1.5.0",
        "org.scala-lang" % "scala-compiler" % "2.10.0",
        //"org.scalacheck" %% "scalacheck" % "1.10.1-SNAPSHOT" cross scheckCrossVersion,
        "net.databinder" %% "dispatch-http" % "0.8.8",
        "org.json4s" %% "json4s-native" % "3.1.0",
        // "org.spire-math"  %% "spire" % "0.7.3" cross crossVersion,
        // "org.spire-math"  %% "spire-scalacheck-binding" % "0.7.3" cross crossVersion,
        // "net.liftweb" %% "lift-util" % "2.5-SNAPSHOT",
        "org.jruby" % "jruby-complete" % "1.7.10"
)}


// autoCompilerPlugins := true

assembleArtifact in packageBin := false

seq(assemblySettings: _*)

scalacOptions ++= Seq("-feature", "-language:_", "-deprecation", "-Xexperimental")

// mergeStrategy in assembly := (e => MergeStrategy.first)

//mergeStrategy in assembly := {
//    case s if s.startsWith("org/joda") => MergeStrategy.discard
//    case _ => MergeStrategy.deduplicate
//}

resolvers += "Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
