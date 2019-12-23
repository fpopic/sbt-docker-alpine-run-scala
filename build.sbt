import sbt.Keys.`package`

name := "my-example"
version := "1.0.0-SNAPSHOT"
organization := "com.github.fpopic"

scalaVersion := "2.12.10"

autoScalaLibrary := false
libraryDependencies += "org.scala-lang" % "scala-library" % scalaVersion.value % Provided

mainClass in(Compile, run) := Some("com.github.fpopic.Main")

enablePlugins(sbtdocker.DockerPlugin, AshScriptPlugin)

excludeFilter in Compile in unmanagedResources := "development.conf" || "production.conf" || "staging.conf"

dockerfile in docker := {
  val jarFile = (`package` in(Compile, packageBin)).value

  // Make a colon separated classpath with the JAR file
  val classpath = (managedClasspath in Compile).value.files
  val classpathString = s"/usr/share/scala/:${classpath.map(_.getName).mkString(":")}:${jarFile.getName}"

  val mainclass = (mainClass in(Compile, packageBin)).value
    .getOrElse(sys.error("Expected exactly one main class!"))

  new Dockerfile {
    val SCALA_VERSION = scalaVersion.value
    val USER_ID = 1001
    val GROUP_ID = 1001

    from("openjdk:8-jre-alpine")
    runRaw(
      Seq(
        // Install required packages
        s"apk update",
        s"apk add --no-cache --virtual=.build-dependencies wget ca-certificates",
        s"apk add --no-cache bash", // needed for scala --version

        // Install Scala
        s"cd /tmp",
        s"wget --no-verbose https://downloads.lightbend.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz",
        s"tar xzf scala-$SCALA_VERSION.tgz",
        s"mkdir -p /usr/share/scala",
        s"rm /tmp/scala-$SCALA_VERSION/bin/*.bat",
        s"mv /tmp/scala-$SCALA_VERSION/bin /tmp/scala-$SCALA_VERSION/lib /usr/share/scala",
        s"apk del .build-dependencies",
        s"rm -rf /tmp/*",
        s"chmod -R 755 /usr/share/scala",
        s"ln -s /usr/share/scala/bin/scala /usr/local/bin/scala",

        // Add non root app:app user
        s"addgroup -g $GROUP_ID -S app",
        s"adduser -H -u $USER_ID -S app -G app",
        s"mkdir /app",
        s"chown -R app:app /app",
        s"scala -version",
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
