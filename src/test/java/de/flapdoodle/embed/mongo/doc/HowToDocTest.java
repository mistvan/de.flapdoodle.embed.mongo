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
package de.flapdoodle.embed.mongo.doc;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongoImportExecutable;
import de.flapdoodle.embed.mongo.MongoImportProcess;
import de.flapdoodle.embed.mongo.MongoImportStarter;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.Defaults;
import de.flapdoodle.embed.mongo.config.MongoCmdOptions;
import de.flapdoodle.embed.mongo.config.MongoImportConfig;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.Storage;
import de.flapdoodle.embed.mongo.config.Timeout;
import de.flapdoodle.embed.mongo.config.processlistener.CopyDbFilesFromDirBeforeProcessStop;
import de.flapdoodle.embed.mongo.distribution.Feature;
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.distribution.Versions;
import de.flapdoodle.embed.mongo.examples.AbstractMongoDBTest;
import de.flapdoodle.embed.mongo.examples.FileStreamProcessor;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.config.store.HttpProxyFactory;
import de.flapdoodle.embed.process.distribution.Distribution;
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
import de.flapdoodle.testdoc.Includes;
import de.flapdoodle.testdoc.Recorder;
import de.flapdoodle.testdoc.Recording;
import de.flapdoodle.testdoc.TabSize;

public class HowToDocTest {

	@ClassRule
	public static final Recording recording=Recorder.with("Howto.md",TabSize.spaces(2));
	
	@Test
	public void testStandard() throws UnknownHostException, IOException {
		recording.begin();
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

			try (MongoClient mongo = new MongoClient("localhost", port)) {
				DB db = mongo.getDB("test");
				DBCollection col = db.createCollection("testCol", new BasicDBObject());
				col.save(new BasicDBObject("testDoc", new Date()));
			}

		} finally {
			if (mongodExecutable != null)
				mongodExecutable.stop();
		}
		recording.end();
	}

	@Test
	public void testCustomMongodFilename() throws UnknownHostException, IOException {
		recording.begin();		
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

			try (MongoClient mongo = new MongoClient("localhost", port)) {
				DB db = mongo.getDB("test");
				DBCollection col = db.createCollection("testCol", new BasicDBObject());
				col.save(new BasicDBObject("testDoc", new Date()));
			}

		} finally {
			if (mongodExecutable != null)
				mongodExecutable.stop();
		}
		recording.end();		
	}

	public void testUnitTests() {
		// @include AbstractMongoDBTest.java
		Class<?> see = AbstractMongoDBTest.class;
	}

	@Test
	public void testMongodForTests() throws IOException {
		recording.begin();
		MongodForTestsFactory factory = null;
		try {
			factory = MongodForTestsFactory.with(Version.Main.PRODUCTION);

			try (MongoClient mongo = factory.newMongo()) {
				DB db = mongo.getDB("test-" + UUID.randomUUID());
				DBCollection col = db.createCollection("testCol", new BasicDBObject());
				col.save(new BasicDBObject("testDoc", new Date()));
			}

		} finally {
			if (factory != null)
				factory.shutdown();
		}
		recording.end();
	}

	@Test
	public void testCustomizeDownloadURL() {
		recording.begin();
		Command command = Command.MongoD;

		RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(command)
				.artifactStore(Defaults.extractedArtifactStoreFor(command)
						.withDownloadConfig(Defaults.downloadConfigFor(command)
								.downloadPath((__) -> "http://my.custom.download.domain/")
								.build()))
				.build();
		recording.end();
	}

	@Test 
	public void testCustomProxy() {
		recording.begin();
		Command command = Command.MongoD;

		RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(command)
				.artifactStore(Defaults.extractedArtifactStoreFor(command)
						.withDownloadConfig(Defaults.downloadConfigFor(command)
								.proxyFactory(new HttpProxyFactory("fooo", 1234))
								.build()))
				.build();
		recording.end();
	}
	
	@Test
	public void testCustomizeArtifactStorage() throws IOException {
		MongodConfig mongodConfig = MongodConfig.builder()
				.version(Version.Main.PRODUCTION)
				.net(new Net(Network.getFreeServerPort(), Network.localhostIsIPv6()))
				.build();

		// ->
		// ...
		recording.begin();
		Directory artifactStorePath = new FixedPath(System.getProperty("user.home") + "/.embeddedMongodbCustomPath");
		TempNaming executableNaming = new UUIDTempNaming();

		Command command = Command.MongoD;		

		RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(command)
				.artifactStore(Defaults.extractedArtifactStoreFor(command)
						.withDownloadConfig(Defaults.downloadConfigFor(command)
								.artifactStorePath(artifactStorePath)
								.build())
						.executableNaming(executableNaming))
				.build();

		MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
		MongodExecutable mongodExe = runtime.prepare(mongodConfig);
		recording.end();
		// ...
		// <-
		MongodProcess mongod = mongodExe.start();

		mongod.stop();
		mongodExe.stop();
	}

	@Test
	public void testCustomOutputToConsolePrefix() {
		// ->
		// ...
		recording.begin();
		ProcessOutput processOutput = new ProcessOutput(Processors.namedConsole("[mongod>]"),
				Processors.namedConsole("[MONGOD>]"), Processors.namedConsole("[console>]"));

		RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(Command.MongoD)
				.processOutput(processOutput)
				.build();

		MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
		recording.end();
		// ...
		// <-
	}

	@Test
	public void testCustomOutputToFile() throws FileNotFoundException, IOException {
		recording.include(FileStreamProcessor.class, Includes.WithoutImports, Includes.WithoutPackage, Includes.Trim);
		// ->
		// ...
		recording.begin();
		StreamProcessor mongodOutput = Processors.named("[mongod>]",
				new FileStreamProcessor(File.createTempFile("mongod", "log")));
		StreamProcessor mongodError = new FileStreamProcessor(File.createTempFile("mongod-error", "log"));
		StreamProcessor commandsOutput = Processors.namedConsole("[console>]");

		RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(Command.MongoD)
				.processOutput(new ProcessOutput(mongodOutput, mongodError, commandsOutput))
				.build();

		MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
		recording.end();
		// ...
		// <-
	}

	@Test
	public void testCustomOutputToLogging() throws FileNotFoundException, IOException {
		// ->
		// ...
		recording.begin();
		Logger logger = LoggerFactory.getLogger(getClass().getName());

		ProcessOutput processOutput = new ProcessOutput(Processors.logTo(logger, Slf4jLevel.INFO), Processors.logTo(logger,
				Slf4jLevel.ERROR), Processors.named("[console>]", Processors.logTo(logger, Slf4jLevel.DEBUG)));
		

		RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(Command.MongoD, logger)
				.processOutput(processOutput)
				.artifactStore(Defaults.extractedArtifactStoreFor(Command.MongoD)
						.download(Defaults.downloadConfigFor(Command.MongoD)
								.progressListener(new Slf4jProgressListener(logger))))
				.build();

		MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
		recording.end();
		// ...
		// <-
	}

	// #### ... to default java logging (the easy way)
	@Test
	public void testDefaultOutputToLogging() throws FileNotFoundException, IOException {
		// ->
		// ...
		recording.begin();
		Logger logger = LoggerFactory.getLogger(getClass().getName());

		RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(Command.MongoD, logger)
				.build();

		MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
		recording.end();
		// ...
		// <-
	}

	// #### ... to null device
	@Test
	public void testDefaultOutputToNone() throws IOException {
		int port = 12345;
		MongodConfig mongodConfig = MongodConfig.builder()
				.version(Versions.withFeatures(genericVersion("2.7.1"), Feature.SYNC_DELAY))
				.net(new Net(port, Network.localhostIsIPv6()))
				.build();
		// ->
		// ...
		recording.begin();
		Logger logger = LoggerFactory.getLogger(getClass().getName());

		RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(Command.MongoD, logger)
				.processOutput(ProcessOutput.getDefaultInstanceSilent())
				.build();

		MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
		recording.end();
		// ...
		// <-
		MongodProcess mongod = null;

		MongodExecutable mongodExecutable = null;
		try {
			mongodExecutable = runtime.prepare(mongodConfig);
			mongod = mongodExecutable.start();

			try (MongoClient mongo = new MongoClient("localhost", port)) {
				DB db = mongo.getDB("test");
				DBCollection col = db.createCollection("testCol", new BasicDBObject());
				col.save(new BasicDBObject("testDoc", new Date()));
			}

		} finally {
			if (mongod != null) {
				mongod.stop();
			}
			if (mongodExecutable != null)
				mongodExecutable.stop();
		}
	}

	private de.flapdoodle.embed.process.distribution.Version genericVersion(String asInDownloadPath) {
		return de.flapdoodle.embed.process.distribution.Version.of(asInDownloadPath);
	}

	// ### Custom Version
	@Test
	public void testCustomVersion() throws IOException {
		// ->
		// ...
		recording.begin();
		int port = 12345;
		MongodConfig mongodConfig = MongodConfig.builder()
				.version(Versions.withFeatures(de.flapdoodle.embed.process.distribution.Version.of("2.7.1"), Feature.SYNC_DELAY))
				.net(new Net(port, Network.localhostIsIPv6()))
				.build();

		MongodStarter runtime = MongodStarter.getDefaultInstance();
		MongodProcess mongod = null;

		MongodExecutable mongodExecutable = null;
		try {
			mongodExecutable = runtime.prepare(mongodConfig);
			mongod = mongodExecutable.start();

			// <-
			recording.end();
			try (MongoClient mongo = new MongoClient("localhost", port)) {
				DB db = mongo.getDB("test");
				DBCollection col = db.createCollection("testCol", new BasicDBObject());
				col.save(new BasicDBObject("testDoc", new Date()));
			}
			recording.begin();
			// ->
			// ...

		} finally {
			if (mongod != null) {
				mongod.stop();
			}
			if (mongodExecutable != null)
				mongodExecutable.stop();
		}
		recording.end();
		// ...
		// <-

	}

	// ### Main Versions
	@Test
	public void testMainVersions() throws UnknownHostException, IOException {
		// ->
		recording.begin();
		IFeatureAwareVersion version = Version.V2_2_5;
		// uses latest supported 2.2.x Version
		version = Version.Main.V2_2;
		// uses latest supported production version
		version = Version.Main.PRODUCTION;
		// uses latest supported development version
		version = Version.Main.DEVELOPMENT;
		recording.end();
		// <-
	}

	// ### Use Free Server Port
	/*
	// ->
		Warning: maybe not as stable, as expected.
	// <-
	 */
	// #### ... by hand
	@Test
	public void testFreeServerPort() throws UnknownHostException, IOException {
		// ->
		// ...
		recording.begin();
		int port = Network.getFreeServerPort();
		recording.end();
		// ...
		// <-
	}

	// #### ... automagic
	@Test
	public void testFreeServerPortAuto() throws UnknownHostException, IOException {
		// ->
		// ...
		recording.begin();
		MongodConfig mongodConfig = MongodConfig.builder().version(Version.Main.PRODUCTION).build();

		MongodStarter runtime = MongodStarter.getDefaultInstance();

		MongodExecutable mongodExecutable = null;
		MongodProcess mongod = null;
		try {
			mongodExecutable = runtime.prepare(mongodConfig);
			mongod = mongodExecutable.start();

			try (MongoClient mongo = new MongoClient(
					new ServerAddress(mongodConfig.net().getServerAddress(), mongodConfig.net().getPort()))) {
			// <-
				recording.end();
				DB db = mongo.getDB("test");
				DBCollection col = db.createCollection("testCol", new BasicDBObject());
				col.save(new BasicDBObject("testDoc", new Date()));
				recording.begin();
			}
			// ->
			// ...

		} finally {
			if (mongod != null) {
				mongod.stop();
			}
			if (mongodExecutable != null)
				mongodExecutable.stop();
		}
		recording.end();
		// ...
		// <-
	}

	// ### ... custom timeouts
	@Test
	public void testCustomTimeouts() throws UnknownHostException, IOException {
		// ->
		// ...
		recording.begin();
		MongodConfig mongodConfig = MongodConfig.builder()
				.version(Version.Main.PRODUCTION)
				.timeout(new Timeout(30000))
				.build();
		recording.end();
		// ...
		// <-
		assertNotNull(mongodConfig);
	}

	// ### Command Line Post Processing
	@Test
	public void testCommandLinePostProcessing() {

		// ->
		// ...
		recording.begin();
		CommandLinePostProcessor postProcessor = // ...
				// <-
				new CommandLinePostProcessor() {
					@Override
					public List<String> process(Distribution distribution, List<String> args) {
						return null;
					}
				};
		recording.end();
		// ->
		recording.begin();
		RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(Command.MongoD)
				.commandLinePostProcessor(postProcessor)
				.build();
		recording.end();
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
	@Test
	public void testCommandLineOptions() throws UnknownHostException, IOException {
		// ->
		recording.begin();
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
		recording.end();
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
	@Test
	public void testSnapshotDbFiles() throws UnknownHostException, IOException {
		File destination = null;
		// ->
		recording.begin();
		MongodConfig mongodConfig = MongodConfig.builder()
				.version(Version.Main.PRODUCTION)
				.processListener(new CopyDbFilesFromDirBeforeProcessStop(destination))
				.cmdOptions(MongoCmdOptions.builder()
						.useDefaultSyncDelay(true)
						.build())
				.build();
		recording.end();
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
	@Test
	public void testCustomDatabaseDirectory() throws UnknownHostException, IOException {
		// ->
		recording.begin();
		Storage replication = new Storage("/custom/databaseDir",null,0);
		
		MongodConfig mongodConfig = MongodConfig.builder()
				.version(Version.Main.PRODUCTION)
				.replication(replication)
				.build();
		recording.end();
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
	
  @Test
  public void importJsonIntoMongoDB() throws UnknownHostException, IOException {
		String jsonFile = Thread.currentThread().getContextClassLoader().getResource("sample.json").toString();
		jsonFile = jsonFile.replaceFirst("file:", "");
		String defaultHost = "localhost";
		
  	recording.begin();
		int defaultConfigPort = Network.getFreeServerPort();
		String database = "importTestDB";
		String collection = "importedCollection";

		MongodConfig mongoConfigConfig = MongodConfig.builder()
				.version(Version.Main.PRODUCTION)
				.net(new Net(defaultConfigPort, Network.localhostIsIPv6()))
				.build();

		MongodExecutable mongodExecutable = MongodStarter.getDefaultInstance().prepare(mongoConfigConfig);
		MongodProcess mongod = mongodExecutable.start();

		try {
			MongoImportConfig mongoImportConfig = MongoImportConfig.builder()
					.version(Version.Main.PRODUCTION)
					.net(new Net(defaultConfigPort, Network.localhostIsIPv6()))
					.databaseName(database)
					.collectionName(collection)
					.isUpsertDocuments(true)
					.isDropCollection(true)
					.isJsonArray(true)
					.importFile(jsonFile)
					.build();

			MongoImportExecutable mongoImportExecutable = MongoImportStarter.getDefaultInstance().prepare(mongoImportConfig);
			MongoImportProcess mongoImport = mongoImportExecutable.start();
			try {
				recording.end();
				MongoClient mongoClient = new MongoClient(defaultHost, defaultConfigPort);
				System.out.println("DB Names: " + mongoClient.getDatabaseNames());
				assertNotNull(mongoClient);
				recording.begin();
			}
			finally {
				mongoImport.stop();
			}
		}
		finally {
			mongod.stop();
		}
		recording.end();
  }
}
