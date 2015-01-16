// import play.Project._

organization := "bizinnov"

name := "corporate_risk"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.1"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.0" % Test

libraryDependencies += "deductions" %% "semantic_forms" % "1.0-SNAPSHOT"
// "org.w3" %%  "jena"

javacOptions ++= Seq("-source","1.7", "-target","1.7")

// resolvers += Resolver.mavenLocal
resolvers += Resolver.file("Local repo", file(System.getProperty("user.home") + "/.ivy2/local"))(Resolver.ivyStylePatterns)
// cf http://stackoverflow.com/questions/16400877/local-dependencies-resolved-by-sbt-but-not-by-play-framework

lazy val myapp = (project in file(".")).enablePlugins(PlayScala)

scalariformSettings
