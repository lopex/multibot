import AssemblyKeys._

name := "multibot210"

version := "1.0"

scalaVersion := "2.10.0-RC2"

libraryDependencies ++= {
    val scalazVersion = "7.0-SNAPSHOT"
    Seq(
	"com.chuusai" %% "shapeless" % "1.2.3-SNAPSHOT" cross CrossVersion.full,
	"org.scalaz" %% "scalaz-core" % scalazVersion cross CrossVersion.full,
	"org.scalaz" %% "scalaz-iteratee" % scalazVersion cross CrossVersion.full,
	"org.scalaz" %% "scalaz-effect" % scalazVersion cross CrossVersion.full,
	"org.scalaz" %% "scalaz-typelevel" % scalazVersion cross CrossVersion.full,
	"pircbot" % "pircbot" % "1.5.0",
	"org.scala-lang" % "scala-compiler" % "2.10.0-RC2",
	"org.scalacheck" %% "scalacheck" % "1.10.1-SNAPSHOT" cross CrossVersion.full
    )
}

// autoCompilerPlugins := true

assembleArtifact in packageBin := false

seq(assemblySettings: _*)

// mergeStrategy in assembly := (e => MergeStrategy.first)

resolvers += "Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Sonatype Nexus Repos" at "https://oss.sonatype.org/content/repositories/releases"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"