# sbt-docker docker + package

Generated docker file using `docker` task looks like:

```dockerfile
FROM openjdk:8-jre-alpine
RUN addgroup -g 1001 -S app && \
	adduser -H -u 1001 -S app -G app && \
	mkdir /app
COPY 0/scala-logging_2.12-3.9.2.jar /app/scala-logging_2.12-3.9.2.jar
COPY 1/slf4j-api-1.7.29.jar /app/slf4j-api-1.7.29.jar
COPY 2/scala-library.jar /app/scala-library.jar
COPY 3/logback-classic-1.2.3.jar /app/logback-classic-1.2.3.jar
COPY 4/scala-reflect.jar /app/scala-reflect.jar
COPY 5/config-1.3.4.jar /app/config-1.3.4.jar
COPY 6/pureconfig-generic_2.12-0.12.1.jar /app/pureconfig-generic_2.12-0.12.1.jar
COPY 7/pureconfig_2.12-0.12.1.jar /app/pureconfig_2.12-0.12.1.jar
COPY 8/shapeless_2.12-2.3.3.jar /app/shapeless_2.12-2.3.3.jar
COPY 9/macro-compat_2.12-1.1.1.jar /app/macro-compat_2.12-1.1.1.jar
COPY 10/logback-core-1.2.3.jar /app/logback-core-1.2.3.jar
COPY 11/pureconfig-core_2.12-0.12.1.jar /app/pureconfig-core_2.12-0.12.1.jar
COPY 12/pureconfig-macros_2.12-0.12.1.jar /app/pureconfig-macros_2.12-0.12.1.jar
COPY 13/sbt-docker-example_2.12-1.0.0-SNAPSHOT.jar /app/app.jar
COPY 14/reference.conf /app/app.conf <====it should be production.conf but excludeFilter removed it?
RUN chown -R app:app /app
ENTRYPOINT ["java", "-Dconfig.file=\/app\/app.conf", "-cp", "\/app\/scala-logging_2.12-3.9.2.jar:\/app\/slf4j-api-1.7.29.jar:\/app\/scala-library.jar:\/app\/logback-classic-1.2.3.jar:\/app\/scala-reflect.jar:\/app\/config-1.3.4.jar:\/app\/pureconfig-generic_2.12-0.12.1.jar:\/app\/pureconfig_2.12-0.12.1.jar:\/app\/shapeless_2.12-2.3.3.jar:\/app\/macro-compat_2.12-1.1.1.jar:\/app\/logback-core-1.2.3.jar:\/app\/pureconfig-core_2.12-0.12.1.jar:\/app\/pureconfig-macros_2.12-0.12.1.jar\/app\/app.conf:\/app\/app.jar", "com.github.fpopic.Main"]
```

All COPY statements can be easily turned into one with `&& \`

```bash
$ tree target/docker/

target/docker/
├── 0
│   └── pureconfig_2.12-0.12.1.jar
├── 1
│   └── slf4j-api-1.7.29.jar
├── 10
│   └── pureconfig-macros_2.12-0.12.1.jar
├── 11
│   └── scala-library.jar
├── 12
│   └── shapeless_2.12-2.3.3.jar
├── 13
│   └── my-example_2.12-1.0.0-SNAPSHOT.jar
├── 14
│   └── reference.conf
├── 2
│   └── pureconfig-core_2.12-0.12.1.jar
├── 3
│   └── config-1.3.4.jar
├── 4
│   └── macro-compat_2.12-1.1.1.jar
├── 5
│   └── logback-classic-1.2.3.jar
├── 6
│   └── scala-reflect.jar
├── 7
│   └── scala-logging_2.12-3.9.2.jar
├── 8
│   └── pureconfig-generic_2.12-0.12.1.jar
├── 9
│   └── logback-core-1.2.3.jar
└── Dockerfile
```

Problem can occur when the classpath dependencies have broken order.
