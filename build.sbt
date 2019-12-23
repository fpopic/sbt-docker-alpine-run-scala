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

mainClass in(Compile, run) := Some("com.github.fpopic.Main")

enablePlugins(DockerPlugin)

// `reference.conf` and `application.conf` will be included,
// one of the excluded configurations will be supplied during runtime in the container
excludeFilter in packageBin in unmanagedResources :=
  "development.conf" || "production.conf" || "staging.conf" // TODO excluded from packaging to jar

dockerfile in docker := {
  val configFileOpt = (unmanagedResources in Compile).value.find(_.getName.endsWith("production.conf"))
  val jarFile = (`package` in(Compile, packageBin)).value

  // Make a colon separated classpath with the JAR file
  val classpathFiles = (managedClasspath in Compile).value.files
  val classpathString = s"${classpathFiles.map(_.getName).mkString(":")}:${jarFile.getName}"

  val mainclass = (mainClass in(Compile, packageBin)).value
    .getOrElse(sys.error("Expected exactly one main class, set `mainClass in(Compile, run)`."))

  new Dockerfile {
    from("openjdk:8-jre-alpine")
    runRaw(
      Seq(
        "addgroup -g 1001 -S app",
        "adduser -H -u 1001 -S app -G app",
        "mkdir /app",
        "chown -R app:app /app",
      ).mkString(" && ")
    )
    workDir("/app")
    copy(classpathFiles, ".")
    copy(jarFile, jarFile.getName)
    if (configFileOpt.nonEmpty) {
      val configFile = configFileOpt.get
      copy(configFile, configFile.getName)
      entryPoint("java", s"-Dconfig.resource=/app/${configFile.getName}", "-cp", classpathString, mainclass)
    }
    else entryPoint("java", "-cp", classpathString, mainclass)
  }
}

imageNames in docker := Seq(
  // Updates the latest tag
  ImageName(s"${developers.value.head.id}/${name.value}:latest"),
  // Sets a name with a tag that contains the project version
  ImageName(s"${developers.value.head.id}/${name.value}:v${version.value}")
)

// make the docker build task depend on sbt packageBin task
docker := {docker dependsOn Compile / packageBin}.value
