scalaVersion := "2.13.2"

libraryDependencies += "io.monix" %% "minitest" % "2.8.2" % "test"

testFrameworks += new TestFramework("minitest.runner.Framework")
