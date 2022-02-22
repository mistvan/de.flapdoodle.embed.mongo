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

import com.google.common.io.Resources;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.commands.MongoImportArguments;
import de.flapdoodle.embed.mongo.commands.MongodArguments;
import de.flapdoodle.embed.mongo.config.Defaults;
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.examples.FileStreamProcessor;
import de.flapdoodle.embed.mongo.packageresolver.Command;
import de.flapdoodle.embed.mongo.transitions.ExecutedMongoImportProcess;
import de.flapdoodle.embed.mongo.transitions.MongoImport;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.embed.mongo.types.DatabaseDir;
import de.flapdoodle.embed.mongo.types.DistributionBaseUrl;
import de.flapdoodle.embed.mongo.util.FileUtils;
import de.flapdoodle.embed.process.config.DownloadConfig;
import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.config.store.HttpProxyFactory;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.StreamProcessor;
import de.flapdoodle.embed.process.nio.directories.PersistentDir;
import de.flapdoodle.embed.process.runtime.Network;
import de.flapdoodle.embed.process.transitions.DownloadPackage;
import de.flapdoodle.reverse.*;
import de.flapdoodle.reverse.transitions.Derive;
import de.flapdoodle.reverse.transitions.Start;
import de.flapdoodle.testdoc.Includes;
import de.flapdoodle.testdoc.Recorder;
import de.flapdoodle.testdoc.Recording;
import de.flapdoodle.testdoc.TabSize;
import de.flapdoodle.types.Try;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class HowToDocTest {

	@RegisterExtension
	public static final Recording recording = Recorder.with("Howto.md", TabSize.spaces(2));

	@Test
	public void testStandard() throws IOException {
		recording.begin();

		try (TransitionWalker.ReachedState<RunningMongodProcess> running = Mongod.instance().start(Version.Main.PRODUCTION)) {
			try (MongoClient mongo = new MongoClient(running.current().getServerAddress())) {
				MongoDatabase db = mongo.getDatabase("test");
				MongoCollection<Document> col = db.getCollection("testCol");
				col.insertOne(new Document("testDoc", new Date()));
				recording.end();
				assertThat(col.countDocuments()).isEqualTo(1L);
				recording.begin();
			}
		}

		recording.end();
	}

	@Test
	public void testCustomMongodFilename() throws IOException {
		recording.begin();
		// TODO REMOVE
		recording.end();
	}

	public void testUnitTests() {
		// TODO REMOVE
	}

	@Test
	public void testMongodForTests() throws IOException {
		recording.begin();
		// TODO REMOVE
		recording.end();
	}

	@Test
	public void testCustomizeDownloadURL() {
		recording.begin();
		Mongod mongod = new Mongod() {
			@Override
			public Transition<DistributionBaseUrl> distributionBaseUrl() {
				return Start.to(DistributionBaseUrl.class)
					.initializedWith(DistributionBaseUrl.of("http://my.custom.download.domain"));
			}
		};
		recording.end();

		assertThatThrownBy(() -> mongod.start(Version.Main.PRODUCTION))
			.isInstanceOf(RuntimeException.class);
	}

	@Test
	public void testCustomProxy() throws UnknownHostException {
		recording.begin();
		Mongod mongod = new Mongod() {
			@Override public DownloadPackage downloadPackage() {
				return DownloadPackage.withDefaults()
					.withDownloadConfig(DownloadConfig.defaults()
						.withProxyFactory(new HttpProxyFactory("fooo", 1234)));
			}
		};
		recording.end();
		try (TransitionWalker.ReachedState<RunningMongodProcess> running = mongod.start(Version.Main.PRODUCTION)) {
			assertRunningMongoDB(running);
		}
	}

	@Test
	public void testCustomizeArtifactStorage() throws UnknownHostException {
		recording.begin();
		Mongod mongod = new Mongod() {
			@Override
			public Transition<PersistentDir> persistentBaseDir() {
				return Start.to(PersistentDir.class)
					.providedBy(PersistentDir.userHome(".embeddedMongodbCustomPath"));
			}
		};
		recording.end();
		try (TransitionWalker.ReachedState<RunningMongodProcess> running = mongod.start(Version.Main.PRODUCTION)) {
			assertRunningMongoDB(running);
		}
	}

	@Test
	public void testCustomOutputToConsolePrefix() throws UnknownHostException {
		recording.begin();
		Mongod mongod = new Mongod() {
			@Override
			public Transition<ProcessOutput> processOutput() {
				return Start.to(ProcessOutput.class)
					.initializedWith(new ProcessOutput(
						Processors.namedConsole("[mongod>]"),
						Processors.namedConsole("[MONGOD>]"),
						Processors.namedConsole("[console>]")
					))
					.withTransitionLabel("create named console");
			}
		};
		recording.end();
		try (TransitionWalker.ReachedState<RunningMongodProcess> running = mongod.start(Version.Main.PRODUCTION)) {
			assertRunningMongoDB(running);
		}
	}

	@Test
	public void testCustomOutputToFile() throws IOException {
		recording.include(FileStreamProcessor.class, Includes.WithoutImports, Includes.WithoutPackage, Includes.Trim);
		recording.begin();
		Mongod mongod = new Mongod() {
			@Override
			public Transition<ProcessOutput> processOutput() {
				return Start.to(ProcessOutput.class)
					.providedBy(Try.supplier(() -> new ProcessOutput(
						Processors.named("[mongod>]",
							new FileStreamProcessor(File.createTempFile("mongod", "log"))),
							new FileStreamProcessor(File.createTempFile("mongod-error", "log")),
							Processors.namedConsole("[console>]")
					)).mapCheckedException(RuntimeException::new)
					::get)
					.withTransitionLabel("create named console");
			}
		};
		recording.end();
		try (TransitionWalker.ReachedState<RunningMongodProcess> running = mongod.start(Version.Main.PRODUCTION)) {
			assertRunningMongoDB(running);
		}
	}

	@Test
	public void testCustomOutputToLogging() {
		recording.begin();
		// TODO remove me
		recording.end();
	}

	@Test
	public void testDefaultOutputToLogging() {
		recording.begin();
		// TODO remove me
		recording.end();
	}

	@Test
	public void testDefaultOutputToNone() throws IOException {
		recording.begin();
		try (TransitionWalker.ReachedState<RunningMongodProcess> running = Mongod.instance().transitions(Version.Main.PRODUCTION)
			.replace(Start.to(ProcessOutput.class)
				.initializedWith(new ProcessOutput(
					Processors.silent(),
					Processors.silent(),
					Processors.silent()
				))
				.withTransitionLabel("no output"))
			.walker().initState(StateID.of(RunningMongodProcess.class))) {

			try (MongoClient mongo = new MongoClient(running.current().getServerAddress())) {
				MongoDatabase db = mongo.getDatabase("test");
				MongoCollection<Document> col = db.getCollection("testCol");
				col.insertOne(new Document("testDoc", new Date()));
				recording.end();
				assertThat(col.countDocuments()).isEqualTo(1L);
				recording.begin();
			}
		}
		recording.end();
	}

	@Test
	public void testCustomVersion() {
		recording.begin();
		// TODO remove
		recording.end();
	}

	@Test
	public void testMainVersions() {
		recording.begin();
		IFeatureAwareVersion version = Version.V2_2_5;
		// uses latest supported 2.2.x Version
		version = Version.Main.V2_2;
		// uses latest supported production version
		version = Version.Main.PRODUCTION;
		// uses latest supported development version
		version = Version.Main.DEVELOPMENT;
		recording.end();
	}

	@Test
	public void testFreeServerPort() throws IOException {
		recording.begin();
		int port = Network.getFreeServerPort();
		recording.end();
	}

	@Test
	public void testFreeServerPortAuto() throws IOException {
		recording.begin();
		// TODO remove me
		recording.end();
	}

	@Test
	public void testCustomTimeouts() throws UnknownHostException, IOException {
		recording.begin();
		// TODO set mongod timeout to 30000
		recording.end();
	}

	@Test
	public void testCommandLinePostProcessing() {
		recording.begin();
		// TODO change command line arguments before calling process start??
		recording.end();
	}

	@Test
	public void testCommandLineOptions() {
		recording.begin();
		new Mongod() {
			@Override
			public Transition<MongodArguments> mongodArguments() {
				return Start.to(MongodArguments.class)
					.initializedWith(MongodArguments.defaults().withSyncDelay(10)
						.withUseNoPrealloc(false)
						.withUseSmallFiles(false)
						.withUseNoJournal(false)
						.withEnableTextSearch(true));
			}
		}.transitions(Version.Main.PRODUCTION);
		recording.end();
	}

	@Test
	public void testSnapshotDbFiles(@TempDir Path destination) {
		recording.begin();

		Listener listener = Listener.typedBuilder()
			.onStateTearDown(StateID.of(DatabaseDir.class), databaseDir -> {
				Try.run(() -> FileUtils.copyDirectory(databaseDir.value(), destination));
			})
			.build();

		try (TransitionWalker.ReachedState<RunningMongodProcess> running = Mongod.instance().transitions(Version.Main.PRODUCTION).walker()
			.initState(StateID.of(RunningMongodProcess.class), listener)) {
		}

		assertThat(destination)
			.isDirectory()
			.isDirectoryContaining(path -> path.getFileName().toString().startsWith("WiredTiger.lock"));

		recording.end();
	}

	@Test
	public void testCustomDatabaseDirectory(@TempDir Path customDatabaseDir) throws UnknownHostException, IOException {
		recording.begin();
		Mongod.instance().transitions(Version.Main.PRODUCTION)
			.replace(Start.to(DatabaseDir.class).initializedWith(DatabaseDir.of(customDatabaseDir)));

		// TODO replication config? replSetName, oplogSize?
		recording.end();
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
	public void importJsonIntoMongoDB() {
		String jsonFile = Resources.getResource("sample.json").getFile();

		recording.begin();

		Version.Main version = Version.Main.PRODUCTION;

		Transitions transitions = MongoImport.instance().transitions(version)
			.replace(Start.to(MongoImportArguments.class).initializedWith(MongoImportArguments.builder()
				.databaseName("importTestDB")
				.collectionName("importedCollection")
				.upsertDocuments(true)
				.dropCollection(true)
				.isJsonArray(true)
				.importFile(jsonFile)
				.build()))
			.addAll(Derive.given(RunningMongodProcess.class).state(ServerAddress.class)
				.deriveBy(Try.function(RunningMongodProcess::getServerAddress).mapCheckedException(RuntimeException::new)::apply))
			.addAll(Mongod.instance().transitions(version).walker()
				.asTransitionTo(TransitionMapping.builder("mongod", StateID.of(RunningMongodProcess.class))
					.build()));

		try (TransitionWalker.ReachedState<RunningMongodProcess> runningMongoD = transitions.walker()
			.initState(StateID.of(RunningMongodProcess.class))) {

			try (TransitionWalker.ReachedState<ExecutedMongoImportProcess> executedImport = runningMongoD.initState(
				StateID.of(ExecutedMongoImportProcess.class))) {

				assertThat(executedImport.current().returnCode())
					.describedAs("import successful")
					.isEqualTo(0);
			}
		}

		recording.end();
	}

	private static void assertRunningMongoDB(TransitionWalker.ReachedState<RunningMongodProcess> running) throws UnknownHostException {
		try (MongoClient mongo = new MongoClient(running.current().getServerAddress())) {
			MongoDatabase db = mongo.getDatabase("test");
			MongoCollection<Document> col = db.getCollection("testCol");
			col.insertOne(new Document("testDoc", new Date()));
			assertThat(col.countDocuments()).isEqualTo(1L);
		}
	}
}
