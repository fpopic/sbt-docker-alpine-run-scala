import java.io.File

import Dependencies._

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

// Need to use full name to DockerPlugin,
// since sbt-native-packager uses the same name for its Docker plugin.
// AshScriptPlugin helps in alpine images that don't have bash installed
enablePlugins(sbtdocker.DockerPlugin, AshScriptPlugin, JavaAppPackaging)

// `reference.conf` and `application.conf` will always be included,
// one of the excluded configurations will be supplied in runtime from the /app/app.conf
excludeFilter in `packageBin` in unmanagedResources :=
  "production.conf" || "staging.conf" || "development.conf" || "local.conf"

dockerfile in docker := {
  val appDir: File = stage.value

  val configFile = {
    // Decide which configuration will be used
    val resources = (unmanagedResources in Compile).value
    val production = resources.find(_.getName.endsWith("production.conf"))
    val application = resources.find(_.getName.endsWith("application.conf"))
    val reference = resources.find(_.getName.endsWith("reference.conf"))
    Seq(production, application, reference).collectFirst { case Some(c) => c }
      .getOrElse(sys.error("Expected at least `reference.conf`!"))
  }

  new Dockerfile {
    from("openjdk:8-jre-alpine")
    runRaw(
      Seq(
        "addgroup -g 1001 -S app",
        "adduser -H -u 1001 -S app -G app",
        "mkdir /app",
      ).mkString(" && \\\n\t")
    )
    copy(configFile, "/app/app.conf")
    copy(appDir, "/app/")
    runRaw("chown -R app:app /app")
    entryPoint(s"/app/bin/${executableScriptName.value}")
  }
}

// add jvm parameter for typesafe config
bashScriptExtraDefines += """addJava "-Dconfig.file=${app_home}/../app.conf""""

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
docker := {docker dependsOn Compile / packageBin} .value



// // try with mappings from project to artifact (in this case docker image)
//mappings in Docker += {
//  ((resourceDirectory in Compile).value / "production.conf") -> "conf/production.conf"
//  ((resourceDirectory in Compile).value / "staging.conf") -> "conf/staging.conf"
//  ((resourceDirectory in Compile).value / "development.conf") -> "conf/development.conf"
//  ((resourceDirectory in Compile).value / "local.conf") -> "conf/local.conf"
//  ((resourceDirectory in Compile).value / "reference.conf") -> "conf/reference.conf"
//  ((resourceDirectory in Compile).value / "applicaiton.conf") -> "conf/application.conf"
//}
