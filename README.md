# sbt-docker + sbt-native-packager JavaAppPackaging

Generated dockerfile using `docker` task looks like:

```dockerfile
FROM openjdk:8-jre-alpine
RUN addgroup -g 1001 -S app && \
	adduser -H -u 1001 -S app -G app && \
	mkdir /app
COPY 0/stage /app/
COPY 1/production.conf 2/staging.conf 3/development.conf /app/conf/
RUN chown -R app:app /app
ENTRYPOINT ["\/app\/bin\/sbt-docker-example"]
```

```bash
$ tree target/docker/

target/docker/
├── 0
│   └── stage
│       ├── bin
│       │   ├── sbt-docker-example
│       │   └── sbt-docker-example.bat
│       └── lib
│           ├── ch.qos.logback.logback-classic-1.2.3.jar
│           ├── ch.qos.logback.logback-core-1.2.3.jar
│           ├── com.chuusai.shapeless_2.12-2.3.3.jar
│           ├── com.github.fpopic.sbt-docker-example-1.0.0-SNAPSHOT.jar
│           ├── com.github.pureconfig.pureconfig_2.12-0.12.1.jar
│           ├── com.github.pureconfig.pureconfig-core_2.12-0.12.1.jar
│           ├── com.github.pureconfig.pureconfig-generic_2.12-0.12.1.jar
│           ├── com.github.pureconfig.pureconfig-macros_2.12-0.12.1.jar
│           ├── com.typesafe.config-1.3.4.jar
│           ├── com.typesafe.scala-logging.scala-logging_2.12-3.9.2.jar
│           ├── org.scala-lang.scala-library-2.12.10.jar
│           ├── org.scala-lang.scala-reflect-2.12.10.jar
│           ├── org.slf4j.slf4j-api-1.7.29.jar
│           └── org.typelevel.macro-compat_2.12-1.1.1.jar
├── 1
│   └── production.conf
├── 2
│   └── staging.conf
├── 3
│   └── development.conf
└── Dockerfile
```

It generates reordered classpath in `docker/0/stage/bin/sbt-docker-example`: 
```bash
app_classpath="$lib_dir/com.github.fpopic.sbt-docker-example-1.0.0-SNAPSHOT.jar:$lib_dir/com.github.pureconfig.pureconfig_2.12-0.12.1.jar:$lib_dir/org.slf4j.slf4j-api-1.7.29.jar:$lib_dir/com.github.pureconfig.pureconfig-core_2.12-0.12.1.jar:$lib_dir/com.typesafe.config-1.3.4.jar:$lib_dir/org.typelevel.macro-compat_2.12-1.1.1.jar:$lib_dir/ch.qos.logback.logback-classic-1.2.3.jar:$lib_dir/org.scala-lang.scala-reflect-2.12.10.jar:$lib_dir/com.typesafe.scala-logging.scala-logging_2.12-3.9.2.jar:$lib_dir/com.github.pureconfig.pureconfig-generic_2.12-0.12.1.jar:$lib_dir/ch.qos.logback.logback-core-1.2.3.jar:$lib_dir/com.github.pureconfig.pureconfig-macros_2.12-0.12.1.jar:$lib_dir/org.scala-lang.scala-library-2.12.10.jar:$lib_dir/com.chuusai.shapeless_2.12-2.3.3.jar"
```
