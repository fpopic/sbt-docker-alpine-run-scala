# sbt-docker-alpine-run-scala

(PoC) Make a sbt setup to build and publish minimal docker image that can run a scala app.   

Would like to exclude environment `{production, staging, development}.conf` configuration files 
from the .jar and provide them from the docker image runtime.

Check the branches.
1. https://github.com/fpopic/sbt-docker-alpine-run-scala/tree/docker-sbt-with-package-task
2. https://github.com/fpopic/sbt-docker-alpine-run-scala/tree/sbt-native-packager-with-java-app-packaging
