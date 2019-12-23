import sbt.Keys.`package`

name := "my-example"
version := "1.0.0-SNAPSHOT"
organization := "com.github.fpopic"

scalaVersion := "2.12.10"

mainClass in(Compile, run) := Some("com.github.fpopic.Main")

enablePlugins(sbtdocker.DockerPlugin, AshScriptPlugin)

excludeFilter in Compile in unmanagedResources := "development.conf" || "production.conf" || "staging.conf"

dockerfile in docker := {
  val jarFile = (`package` in(Compile, packageBin)).value

  // Make a colon separated classpath with the JAR file
  val classpath = (managedClasspath in Compile).value.files
  val classpathString = s"${classpath.map(_.getName).mkString(":")}:${jarFile.getName}"

  val mainclass = (mainClass in(Compile, packageBin)).value
    .getOrElse(sys.error("Expected exactly one main class!"))

  new Dockerfile {
    from("openjdk:8-jre-alpine")
    runRaw(
      Seq(
        s"addgroup -g 1001 -S app",
        s"adduser -H -u 1001 -S app -G app",
        s"mkdir /app",
        s"chown -R app:app /app",
      ).mkString(" && ")
    )
    workDir("/app")
    copy(classpath, ".")
    copy(jarFile, jarFile.getName)
    entryPoint("java", "-cp", classpathString, mainclass)
  }
}

imageNames in docker := Seq(
  // Updates the latest tag
  ImageName(s"${organization.value}/${name.value}:latest"),
  // Sets a name with a tag that contains the project version
  ImageName(s"${organization.value}/${name.value}:v${version.value}")
)

// make the docker build task depends on sbt package task
docker := {docker dependsOn Compile / packageBin}.value

Global / onChangedBuildSource := ReloadOnSourceChanges
