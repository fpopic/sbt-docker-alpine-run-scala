import java.io.File

import Dependencies._
import sbt.Keys.`package`

Global / onChangedBuildSource := ReloadOnSourceChanges

name := "sbt-docker-example"
version := "1.0.0-SNAPSHOT"
organization := "com.github.fpopic"
developers := Developer(
  id = "fpopic",
  name = "Filip Popic",
  email = "filip.popic@gmail.com",
  url = url("https://github.com/fpopic")) :: Nil
scalaVersion := "2.12.10"

// added few dependencies to check generated classpath order
libraryDependencies ++= Seq(pureconfig, slf4jApi, logbackClassic, scalaLogging)

// define main class used as docker image entrypoint
mainClass in(Compile, run) := Some("com.github.fpopic.Main")

enablePlugins(DockerPlugin)

// `reference.conf` and `application.conf` will always be included,
// one of the excluded configurations will be supplied in runtime from the /app/app.conf
excludeFilter in `packageBin` in unmanagedResources :=
  "production.conf" || "staging.conf" || "development.conf" || "local.conf"

dockerfile in docker := {
  // Get app's jar file and main class
  val jarFile = (`package` in(Compile, packageBin)).value
  val mainClazz = (mainClass in(Compile, packageBin)).value
    .getOrElse(sys.error("Expected exactly one main class, set `mainClass in(Compile, run)`!"))

  // Make a colon separated classpath with the app's jar and conf file at the end
  val classpathFiles = (managedClasspath in Compile).value.files
  val classpathStringWithConfAndJar =
    s"${classpathFiles.map("/app/" + _.getName).mkString(":")}/app/app.conf:/app/app.jar"

  // Get configuration files
  val confDir = Seq(
    resourceDirectory.in(Compile).value / "production.conf",
    resourceDirectory.in(Compile).value / "staging.conf",
    resourceDirectory.in(Compile).value / "development.conf",
    resourceDirectory.in(Compile).value / "local.conf",
  ).filter(_.exists)

  new Dockerfile {
    from("openjdk:8-jre-alpine")
    runRaw(
      Seq(
        "addgroup -g 1001 -S app",
        "adduser -H -u 1001 -S app -G app",
        "mkdir /app",
      ).mkString(" && \\\n\t")
    )
    copy(classpathFiles, s"/app/")
    copy(jarFile, "/app/app.jar")
    copy(confDir, "/app/conf/")
    runRaw("chown -R app:app /app")
    entryPoint("java", "-Dconfig.file=/app/conf/production.conf", "-cp", classpathStringWithConfAndJar, mainClazz)
  }
}

imageNames in docker := Seq(
  // Updates the latest tag
  ImageName(s"${developers.value.head.id}/${name.value}:latest"),
  // Sets a name with a tag that contains the project version
  ImageName(s"${developers.value.head.id}/${name.value}:v${version.value}")
)

buildOptions in docker := BuildOptions(
  cache = true,
  removeIntermediateContainers = BuildOptions.Remove.Always,
  pullBaseImage = BuildOptions.Pull.IfMissing
)

// make the docker build task depend on sbt packageBin task
docker := {docker dependsOn Compile / packageBin}.value
