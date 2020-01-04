# sbt-docker docker + package

Generated docker file using `docker` task looks like:

```dockerfile
FROM openjdk:8-jre-alpine
RUN addgroup -g 1001 -S app && \
    adduser -H -u 1001 -S app -G app && \
    mkdir /app
COPY 0/pureconfig_2.12-0.12.1.jar 1/slf4j-api-1.7.29.jar 2/pureconfig-core_2.12-0.12.1.jar 3/config-1.3.4.jar 4/macro-compat_2.12-1.1.1.jar 5/logback-classic-1.2.3.jar 6/scala-reflect.jar 7/scala-logging_2.12-3.9.2.jar 8/pureconfig-generic_2.12-0.12.1.jar 9/logback-core-1.2.3.jar 10/pureconfig-macros_2.12-0.12.1.jar 11/scala-library.jar 12/shapeless_2.12-2.3.3.jar /app/
COPY 13/sbt-docker-example_2.12-1.0.0-SNAPSHOT.jar /app/app.jar
COPY 14/production.conf 15/staging.conf 16/development.conf /app/conf/
RUN chown -R app:app /app
ENTRYPOINT ["java", "-Dconfig.file=\/app\/conf\/production.conf", "-cp", "\/app\/pureconfig_2.12-0.12.1.jar:\/app\/slf4j-api-1.7.29.jar:\/app\/pureconfig-core_2.12-0.12.1.jar:\/app\/config-1.3.4.jar:\/app\/macro-compat_2.12-1.1.1.jar:\/app\/logback-classic-1.2.3.jar:\/app\/scala-reflect.jar:\/app\/scala-logging_2.12-3.9.2.jar:\/app\/pureconfig-generic_2.12-0.12.1.jar:\/app\/logback-core-1.2.3.jar:\/app\/pureconfig-macros_2.12-0.12.1.jar:\/app\/scala-library.jar:\/app\/shapeless_2.12-2.3.3.jar\/app\/app.conf:\/app\/app.jar", "com.github.fpopic.Main"]```
```

All COPY statements can be easily turned into one with `&& \` and vice versa.

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
│   └── sbt-docker-example_2.12-1.0.0-SNAPSHOT.jar
├── 14
│   └── production.conf
├── 15
│   └── staging.conf
├── 16
│   └── development.conf
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
