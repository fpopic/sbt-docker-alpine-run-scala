# sbt-docker-alpine-run-scala
(PoC) Make a sbt setup to build and publish minimal docker image that can run a scala app.   

Would like to exclude environment `{production, staging, development}.conf` configuration files from the `packageBin` task and provide them in the dockerfile entrypoint cmd.

Generated docker file using `sbt-docker` + `package` task` looks like:

```dockerfile
```

Problem can occur when the classpath dependencies have broken order.
