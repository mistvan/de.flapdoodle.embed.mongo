/**
 * Copyright (C) 2011
 *   Michael Mosmann <michael@mosmann.de>
 *   Martin Jöhren <m.joehren@googlemail.com>
 *
 * with contributions from
 * 	konstantin-ba@github,Archimedes Trajano	(trajano@github)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.flapdoodle.embed.mongo.examples;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.Defaults;
import de.flapdoodle.embed.mongo.config.MongoCmdOptions;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.Storage;
import de.flapdoodle.embed.mongo.config.Timeout;
import de.flapdoodle.embed.mongo.config.processlistener.CopyDbFilesFromDirBeforeProcessStop;
import de.flapdoodle.embed.mongo.distribution.Feature;
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.distribution.Versions;
import de.flapdoodle.embed.mongo.doc.HowToDocTest;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.config.store.HttpProxyFactory;
import de.flapdoodle.embed.process.extract.TempNaming;
import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import de.flapdoodle.embed.process.extract.UserTempNaming;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.Slf4jLevel;
import de.flapdoodle.embed.process.io.StreamProcessor;
import de.flapdoodle.embed.process.io.directories.Directory;
import de.flapdoodle.embed.process.io.directories.FixedPath;
import de.flapdoodle.embed.process.io.progress.Slf4jProgressListener;
import de.flapdoodle.embed.process.runtime.CommandLinePostProcessor;
import de.flapdoodle.embed.process.runtime.Network;

/**
 * @see HowToDocTest
 * @author mosmann
 *
 */
@Deprecated
public class TestExampleReadMeCode /*extends TestCase*/ {

	// ### Usage
	public void testStandard() throws IOException {
		// ->
		MongodStarter starter = MongodStarter.getDefaultInstance();

		int port = Network.getFreeServerPort();
		MongodConfig mongodConfig = MongodConfig.builder()
				.version(Version.Main.PRODUCTION)
				.net(new Net(port, Network.localhostIsIPv6()))
				.build();

		MongodExecutable mongodExecutable = null;
		try {
			mongodExecutable = starter.prepare(mongodConfig);
			MongodProcess mongod = mongodExecutable.start();

			MongoClient mongo = new MongoClient("localhost", port);
			DB db = mongo.getDB("test");
			DBCollection col = db.createCollection("testCol", new BasicDBObject());
			col.save(new BasicDBObject("testDoc", new Date()));

		} finally {
			if (mongodExecutable != null)
				mongodExecutable.stop();
		}
		// <-
	}

	// ### Usage - Optimization
	/*
	// ->
 		You should make the MongodStarter instance or the RuntimeConfig instance static (per Class or per JVM).
 		The main purpose of that is the caching of extracted executables and library files. This is done by the ArtifactStore instance
 		configured with the RuntimeConfig instance. Each instance uses its own cache so multiple RuntimeConfig instances will use multiple
 		ArtifactStores an multiple caches with much less cache hits:)  
	// <-
	 */
	
	// ### Usage - custom mongod filename 
	/*
	// ->
		To avoid windows firewall dialog popups you can chose a stable executable name with UserTempNaming. 
		This way the firewall dialog only popup once any your done. See [Executable Naming](#executable-naming) 
	// <-
	 */
	public void testCustomMongodFilename() throws IOException {
		// ->		
		int port = Network.getFreeServerPort();

		Command command = Command.MongoD;

		RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(command)
		.artifactStore(Defaults.extractedArtifactStoreFor(command)
				.withDownloadConfig(Defaults.downloadConfigFor(command).build())
				.executableNaming(new UserTempNaming()))
		.build();

		MongodConfig mongodConfig = MongodConfig.builder()
				.version(Version.Main.PRODUCTION)
				.net(new Net(port, Network.localhostIsIPv6()))
				.build();

		MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);

		MongodExecutable mongodExecutable = null;
		try {
			mongodExecutable = runtime.prepare(mongodConfig);
			MongodProcess mongod = mongodExecutable.start();

			MongoClient mongo = new MongoClient("localhost", port);
			DB db = mongo.getDB("test");
			DBCollection col = db.createCollection("testCol", new BasicDBObject());
			col.save(new BasicDBObject("testDoc", new Date()));

		} finally {
			if (mongodExecutable != null)
				mongodExecutable.stop();
		}
		// <-
	}

	// ### Unit Tests
	public void testUnitTests() {
		// @include AbstractMongoDBTest.java
		Class<?> see = AbstractMongoDBTest.class;
	}

	// #### ... with some more help
	public void testMongodForTests() throws IOException {
		// ->
		// ...
		MongodForTestsFactory factory = null;
		try {
			factory = MongodForTestsFactory.with(Version.Main.PRODUCTION);

			MongoClient mongo = factory.newMongo();
			DB db = mongo.getDB("test-" + UUID.randomUUID());
			DBCollection col = db.createCollection("testCol", new BasicDBObject());
			col.save(new BasicDBObject("testDoc", new Date()));

		} finally {
			if (factory != null)
				factory.shutdown();
		}
		// ...
		// <-
	}

	// ### Customize Download URL
	public void testCustomizeDownloadURL() {
		// ->
		// ...
		Command command = Command.MongoD;
		

		RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(command)
				.artifactStore(Defaults.extractedArtifactStoreFor(command)
						.download(Defaults.downloadConfigFor(command)
								.downloadPath((__) -> "http://my.custom.download.domain/")))
				.build();
		// ...
		// <-
	}

	// ### Customize Proxy for Download 
	public void testCustomProxy() {
		// ->
		// ...
		Command command = Command.MongoD;

		RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(command)
				.artifactStore(Defaults.extractedArtifactStoreFor(command)
						.download(Defaults.downloadConfigFor(command)
								.proxyFactory(new HttpProxyFactory("fooo", 1234))))
				.build();
		// ...
		// <-
	}
	
	// ### Customize Artifact Storage
	public void testCustomizeArtifactStorage() throws IOException {

		MongodConfig mongodConfig = MongodConfig.builder()
				.version(Version.Main.PRODUCTION)
				.net(new Net(Network.getFreeServerPort(), Network.localhostIsIPv6()))
				.build();

		// ->
		// ...
		Directory artifactStorePath = new FixedPath(System.getProperty("user.home") + "/.embeddedMongodbCustomPath");
		TempNaming executableNaming = new UUIDTempNaming();

		Command command = Command.MongoD;
		

		RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(command)
				.artifactStore(Defaults.extractedArtifactStoreFor(command)
						.download(Defaults.downloadConfigFor(command)
								.artifactStorePath(artifactStorePath))
						.executableNaming(executableNaming))
				.build();

		MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
		MongodExecutable mongodExe = runtime.prepare(mongodConfig);
		// ...
		// <-
		MongodProcess mongod = mongodExe.start();

		mongod.stop();
		mongodExe.stop();
	}

	// ### Usage - custom mongod process output
	// #### ... to console with line prefix
	public void testCustomOutputToConsolePrefix() {
		// ->
		// ...
		ProcessOutput processOutput = new ProcessOutput(Processors.namedConsole("[mongod>]"),
				Processors.namedConsole("[MONGOD>]"), Processors.namedConsole("[console>]"));

		RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(Command.MongoD)
				.processOutput(processOutput)
				.build();

		MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
		// ...
		// <-
	}

	// #### ... to file
	public void testCustomOutputToFile() throws IOException {
		// ->
		// ...
		StreamProcessor mongodOutput = Processors.named("[mongod>]",
				new FileStreamProcessor(File.createTempFile("mongod", "log")));
		StreamProcessor mongodError = new FileStreamProcessor(File.createTempFile("mongod-error", "log"));
		StreamProcessor commandsOutput = Processors.namedConsole("[console>]");

		RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(Command.MongoD)
				.processOutput(new ProcessOutput(mongodOutput, mongodError, commandsOutput))
				.build();

		MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
		// ...
		// <-
	}

	/*
	 * Ist fürs Readme, deshalb nicht statisch und public
	 */
	// ->

	// ...
	
	// ...
	// <-

	// #### ... to java logging
	public void testCustomOutputToLogging() throws IOException {
		// ->
		// ...
		Logger logger = LoggerFactory.getLogger(getClass().getName());

		ProcessOutput processOutput = new ProcessOutput(Processors.logTo(logger, Slf4jLevel.INFO), Processors.logTo(logger,
				Slf4jLevel.ERROR), Processors.named("[console>]", Processors.logTo(logger, Slf4jLevel.DEBUG)));

		RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(Command.MongoD, logger)
				.processOutput(processOutput)
				.artifactStore(Defaults.extractedArtifactStoreFor(Command.MongoD)
						.download(Defaults.downloadConfigFor(Command.MongoD)
								.progressListener(new Slf4jProgressListener(logger, Slf4jLevel.DEBUG))))
				.build();

		MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
		// ...
		// <-
	}

	// #### ... to default java logging (the easy way)
	public void testDefaultOutputToLogging() throws IOException {
		// ->
		// ...
		Logger logger = LoggerFactory.getLogger(getClass().getName());

		RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(Command.MongoD, logger)
				.build();

		MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
		// ...
		// <-
	}

	// #### ... to null device
	public void testDefaultOutputToNone() throws IOException {
		int port = 12345;
		MongodConfig mongodConfig = MongodConfig.builder()
				.version(Versions.withFeatures(de.flapdoodle.embed.process.distribution.Version.of("2.0.7-rc1"), Feature.SYNC_DELAY))
				.net(new Net(port, Network.localhostIsIPv6()))
				.build();
		// ->
		// ...
		Logger logger = LoggerFactory.getLogger(getClass().getName());

		RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(Command.MongoD, logger)
				.processOutput(ProcessOutput.getDefaultInstanceSilent())
				.build();

		MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
		// ...
		// <-
		MongodProcess mongod = null;

		MongodExecutable mongodExecutable = null;
		try {
			mongodExecutable = runtime.prepare(mongodConfig);
			mongod = mongodExecutable.start();

			MongoClient mongo = new MongoClient("localhost", port);
			DB db = mongo.getDB("test");
			DBCollection col = db.createCollection("testCol", new BasicDBObject());
			col.save(new BasicDBObject("testDoc", new Date()));

		} finally {
			if (mongod != null) {
				mongod.stop();
			}
			if (mongodExecutable != null)
				mongodExecutable.stop();
		}
	}

	// ### Custom Version
	public void testCustomVersion() throws IOException {
		// ->
		// ...
		int port = 12345;
		MongodConfig mongodConfig = MongodConfig.builder()
				.version(Versions.withFeatures(genericVersion("2.0.7-rc1"), Feature.SYNC_DELAY))
				.net(new Net(port, Network.localhostIsIPv6()))
				.build();

		MongodStarter runtime = MongodStarter.getDefaultInstance();
		MongodProcess mongod = null;

		MongodExecutable mongodExecutable = null;
		try {
			mongodExecutable = runtime.prepare(mongodConfig);
			mongod = mongodExecutable.start();

			// <-
			MongoClient mongo = new MongoClient("localhost", port);
			DB db = mongo.getDB("test");
			DBCollection col = db.createCollection("testCol", new BasicDBObject());
			col.save(new BasicDBObject("testDoc", new Date()));
			// ->
			// ...

		} finally {
			if (mongod != null) {
				mongod.stop();
			}
			if (mongodExecutable != null)
				mongodExecutable.stop();
		}
		// ...
		// <-

	}

	private de.flapdoodle.embed.process.distribution.Version genericVersion(String asInDownloadPath) {
		return de.flapdoodle.embed.process.distribution.Version.of(asInDownloadPath);
	}

	// ### Main Versions
	public void testMainVersions() throws IOException {
		// ->
		IFeatureAwareVersion version = Version.V2_2_5;
		// uses latest supported 2.2.x Version
		version = Version.Main.V2_2;
		// uses latest supported production version
		version = Version.Main.PRODUCTION;
		// uses latest supported development version
		version = Version.Main.DEVELOPMENT;
		// <-
	}

	// ### Use Free Server Port
	/*
	// ->
		Warning: maybe not as stable, as expected.
	// <-
	 */
	// #### ... by hand
	public void testFreeServerPort() throws IOException {
		// ->
		// ...
		int port = Network.getFreeServerPort();
		// ...
		// <-
	}

	// #### ... automagic
	public void testFreeServerPortAuto() throws IOException {
		// ->
		// ...
		MongodConfig mongodConfig = MongodConfig.builder().version(Version.Main.PRODUCTION).build();

		MongodStarter runtime = MongodStarter.getDefaultInstance();

		MongodExecutable mongodExecutable = null;
		MongodProcess mongod = null;
		try {
			mongodExecutable = runtime.prepare(mongodConfig);
			mongod = mongodExecutable.start();

			MongoClient mongo = new MongoClient(
					new ServerAddress(mongodConfig.net().getServerAddress(), mongodConfig.net().getPort()));
			// <-
			DB db = mongo.getDB("test");
			DBCollection col = db.createCollection("testCol", new BasicDBObject());
			col.save(new BasicDBObject("testDoc", new Date()));
			// ->
			// ...

		} finally {
			if (mongod != null) {
				mongod.stop();
			}
			if (mongodExecutable != null)
				mongodExecutable.stop();
		}
		// ...
		// <-
	}

	// ### ... custom timeouts
	public void testCustomTimeouts() throws IOException {
		// ->
		// ...
		MongodConfig mongodConfig = MongodConfig.builder()
				.version(Version.Main.PRODUCTION)
				.timeout(new Timeout(30000))
				.build();
		// ...
		// <-
		assertNotNull(mongodConfig);
	}

	// ### Command Line Post Processing
	public void testCommandLinePostProcessing() {

		// ->
		// ...
		CommandLinePostProcessor postProcessor = // ...
				// <-
				(distribution, args) -> null;
		// ->

		RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(Command.MongoD)
				.commandLinePostProcessor(postProcessor)
				.build();
		// ...
		// <-
		assertNotNull(runtimeConfig);
	}

	// ### Custom Command Line Options
	/*
	// ->
		We changed the syncDelay to 0 which turns off sync to disc. To turn on default value used defaultSyncDelay().
	// <-
	 */
	public void testCommandLineOptions() throws IOException {
		// ->
		MongodConfig mongodConfig = MongodConfig.builder()
				.version(Version.Main.PRODUCTION)
				.cmdOptions(MongoCmdOptions.builder()
						.syncDelay(10)
						.useNoPrealloc(false)
						.useSmallFiles(false)
						.useNoJournal(false)
						.enableTextSearch(true)
						.build())
				.build();
		// ...
		// <-
		assertNotNull(mongodConfig);
	}

	// ### Snapshot database files from temp dir
	/*
	// ->
		We changed the syncDelay to 0 which turns off sync to disc. To get the files to create an snapshot you must turn on default value (use defaultSyncDelay()).
	// <-
	 */
	public void testSnapshotDbFiles() throws IOException {
		File destination = null;
		// ->
		MongodConfig mongodConfig = MongodConfig.builder()
				.version(Version.Main.PRODUCTION)
				.processListener(new CopyDbFilesFromDirBeforeProcessStop(destination))
				.cmdOptions(MongoCmdOptions.builder()
						.useDefaultSyncDelay(true)
						.build())
				.build();
		// ...
		// <-
		assertNotNull(mongodConfig);
	}
	// ### Custom database directory  
	/*
	// ->
		If you set a custom database directory, it will not be deleted after shutdown
	// <-
	 */
	public void testCustomDatabaseDirectory() throws IOException {
		// ->
		Storage replication = new Storage("/custom/databaseDir",null,0);
		
		MongodConfig mongodConfig = MongodConfig.builder()
				.version(Version.Main.PRODUCTION)
				.replication(replication)
				.build();
		// ...
		// <-
		assertNotNull(mongodConfig);
	}
	// ### Start mongos with mongod instance
	// @include StartConfigAndMongoDBServerTest.java
	
	// ## Common Errors
	
	// ### Executable Collision

	/*
	// ->
	There is a good chance of filename collisions if you use a custom naming schema for the executable (see [Usage - custom mongod filename](#usage---custom-mongod-filename)).
	If you got an exception, then you should make your RuntimeConfig or MongoStarter class or jvm static (static final in your test class or singleton class for all tests).
	// <-
	*/
	
}
