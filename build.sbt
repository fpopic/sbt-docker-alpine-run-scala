import java.io.File

import Dependencies._
import sbt.Keys.`package`

Global / onChangedBuildSource := ReloadOnSourceChanges

name := "my-example"
version := "1.0.0-SNAPSHOT"
organization := "com.github.fpopic"
developers := Developer(
  id = "fpopic",
  name = "Filip Popic",
  email = "filip.popic@gmail.com",
  url = url("https://github.com/fpopic")) :: Nil
scalaVersion := "2.12.10"

libraryDependencies ++= Seq(pureconfig, slf4jApi, logbackClassic, scalaLogging)

// define main class used as docker image entrypoint
mainClass in(Compile, run) := Some("com.github.fpopic.Main")

enablePlugins(DockerPlugin)

// `reference.conf` and `application.conf` will be included,
// one of the excluded configurations will be supplied during runtime in the container
excludeFilter in packageBin in unmanagedResources :=
  "development.conf" || "production.conf" || "staging.conf"

dockerfile in docker := {
  // Get app's jar file and main class
  val jarFile = (`package` in(Compile, packageBin)).value
  val mainClazz = (mainClass in(Compile, packageBin)).value
    .getOrElse(sys.error("Expected exactly one main class, set `mainClass in(Compile, run)`!"))

  // Decide which configuration will be used
  val resources = (unmanagedResources in Compile).value
  val production = resources.find(_.getName.endsWith("production.conf"))
  val reference = resources.find(_.getName.endsWith("reference.conf"))
  val configFile = Seq(production, reference).collectFirst { case Some(c) => c }
    .getOrElse(sys.error("Expected at least `reference.conf`!"))

  // Make a colon separated classpath with the app's jar and conf file at the end
  val classpathFiles = (managedClasspath in Compile).value.files
  val classpathStringWithConfAndJar =
    s"${classpathFiles.map("/app/" + _.getName).mkString(":")}/app/app.conf:/app/app.jar"

  new Dockerfile {
    from("openjdk:8-jre-alpine")
    runRaw(
      Seq(
        "addgroup -g 1001 -S app",
        "adduser -H -u 1001 -S app -G app",
        "mkdir /app",
      ).mkString(" && \\\n\t")
    )
    classpathFiles.foreach { file =>
      copy(file, s"/app/${file.getName}")
    }
    copy(jarFile, "/app/app.jar")
    copy(configFile, "/app/app.conf")
    runRaw("chown -R app:app /app")
    entryPoint("java", "-Dconfig.file=/app/app.conf", "-cp", classpathStringWithConfAndJar, mainClazz)
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


// TODO ensure correct classpath order

//https://github.com/sbt/sbt-native-packager/blob/master/test-project-docker/build.sbt
//  val appDir: File = stage.value

// exclu
//mappings in (Compile, packageBin) += {
//
//  (unmanagedResourceDirectories in (Compile, packageBin)).value
//    .flatten.filter(_.getName.endsWith(".conf").map
//
//
//    //foreach{
//
//  } / "in" / "example.txt") -> "out/example.txt"
//}
