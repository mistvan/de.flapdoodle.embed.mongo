# Make Peace, No War!


# Organization Flapdoodle OSS
[![Build Status](https://travis-ci.org/flapdoodle-oss/de.flapdoodle.embed.mongo.svg?branch=embed-mongo-4.x)](https://travis-ci.org/flapdoodle-oss/de.flapdoodle.embed.mongo)
[![Maven Central](https://img.shields.io/maven-central/v/de.flapdoodle.embed/de.flapdoodle.embed.mongo.svg)](https://maven-badges.herokuapp.com/maven-central/de.flapdoodle.embed/de.flapdoodle.embed.mongo)

We are now a github organization. You are invited to participate.
Starting with version 2 we are going to support only java 8 or higher. If you are looking for the older version you can find it in the 1.7 branch.

# Embedded MongoDB

Embedded MongoDB will provide a platform neutral way for running mongodb in unittests.

## Why?

- dropping databases causing some pains (often you have to wait long time after each test)
- its easy, much easier as installing right version by hand
- you can change version per test

## How?

- download mongodb (and cache it)
- extract it (and cache it)
- java uses its process api to start and monitor the mongo process
- you run your tests
- java kills the mongo process


## License

We use http://www.apache.org/licenses/LICENSE-2.0

## We need your help?

Poll: [Which MongoDB version should stay supported?](https://docs.google.com/forms/d/1Iu8Gy4W0dPfwsE2czoPJAGtYijjmfcZISgb7pU-dZ9U/viewform?usp=send_form)

## Dependencies

### Build on top of

- Embed Process Util [de.flapdoodle.embed.process](https://github.com/flapdoodle-oss/de.flapdoodle.embed.process)

### Other ways to use Embedded MongoDB

- in a Maven build using [maven-mongodb-plugin](https://github.com/Syncleus/maven-mongodb-plugin) or [embedmongo-maven-plugin](https://github.com/joelittlejohn/embedmongo-maven-plugin)
- in a Clojure/Leiningen project using [lein-embongo](https://github.com/joelittlejohn/lein-embongo)
- in a Gradle build using [gradle-mongo-plugin](https://github.com/sourcemuse/GradleMongoPlugin)
- in a Scala/specs2 specification using [specs2-embedmongo](https://github.com/athieriot/specs2-embedmongo)
- in Scala tests using [scalatest-embedmongo](https://github.com/SimplyScala/scalatest-embedmongo)

### Comments about Embedded MongoDB in the Wild

- http://stackoverflow.com/questions/6437226/embedded-mongodb-when-running-integration-tests
- http://blog.diabol.se/?p=390

### Other MongoDB Stuff

- https://github.com/thiloplanz/jmockmongo - mongodb mocking
- https://github.com/lordofthejars/nosql-unit - extended nosql unit testing
- https://github.com/jirutka/embedmongo-spring - Spring Factory Bean for EmbedMongo

### Backward binary compatibility and API changes

There is a report on backward binary compatibility and API changes for the library: https://abi-laboratory.pro/java/tracker/timeline/de.flapdoodle.embed.mongo/ -> thanks @lvc

## Howto

[Usage](Howto.md)

### Maven

Snapshots (Repository http://oss.sonatype.org/content/repositories/snapshots)

	<dependency>
		<groupId>de.flapdoodle.embed</groupId>
		<artifactId>de.flapdoodle.embed.mongo</artifactId>
		<version>4.0.7-beta-SNAPSHOT</version>
	</dependency>

### Gradle

Make sure you have mavenCentral() in your repositories or that your enterprise/local server proxies the maven central repository.

	dependencies {
		testCompile group: "de.flapdoodle.embed", name: "de.flapdoodle.embed.mongo", version: "4.0.7-beta-SNAPSHOT"
	}

### Build from source

When you fork or clone our branch you should always be able to build the library by running

	mvn package

### Changelog

[Changelog](Changelog.md)

### Supported Versions

Versions: some older, a stable and a development version
Support for Linux, Windows and MacOSX.

### Spring Integration

As the spring projects
[removed the embed mongo support in 2.7.0](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.7-Release-Notes#springmongodbembeddedfeatures-configuration-property-removed)
you should consider to use one of these integration projects.
It should behave mostly like the original spring integration, but there are some minor differences:
- version in 'spring.mongodb.embedded.version' is used in package resolver and is not matched against version enum.
- 'spring.mongodb.embedded.features' is not supported (not the way to change the config of mongodb)

If you have any trouble in using them fell free to create an issue.

- [Spring 2.6.x](https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo.spring/tree/spring-2.6.x--embed-mongo-4.x)
- [Spring 2.7.x](https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo.spring/tree/spring-2.7.x--embed-mongo-4.x)

----

YourKit is kindly supporting open source projects with its full-featured Java Profiler.
YourKit, LLC is the creator of innovative and intelligent tools for profiling
Java and .NET applications. Take a look at YourKit's leading software products:
<a href="http://www.yourkit.com/java/profiler/index.jsp">YourKit Java Profiler</a> and
<a href="http://www.yourkit.com/.net/profiler/index.jsp">YourKit .NET Profiler</a>.
