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
package de.flapdoodle.embed.mongo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import de.flapdoodle.embed.mongo.commands.ImmutableMongoDumpArguments;
import de.flapdoodle.embed.mongo.commands.ImmutableMongoRestoreArguments;
import de.flapdoodle.embed.mongo.commands.MongoDumpArguments;
import de.flapdoodle.embed.mongo.commands.MongoRestoreArguments;
import de.flapdoodle.embed.mongo.config.Defaults;
import de.flapdoodle.embed.mongo.config.MongoRestoreConfig;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.RunningMongoDumpProcess;
import de.flapdoodle.embed.mongo.transitions.RunningMongoRestoreProcess;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.io.progress.ProgressListeners;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;
import de.flapdoodle.embed.process.runtime.Network;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.TransitionMapping;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.Transitions;
import de.flapdoodle.reverse.transitions.Derive;
import de.flapdoodle.reverse.transitions.Start;
import de.flapdoodle.types.Try;
import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

import static de.flapdoodle.embed.mongo.TestUtils.getCmdOptions;
import static org.assertj.core.api.Assertions.assertThat;

public class MongoRestoreExecutableTest {

	private static final Logger _logger = LoggerFactory.getLogger(MongoRestoreExecutableTest.class.getName());
	private static final String _archiveFileCompressed = "foo.archive.gz";

	private static void dumpAndRestore(
		Version.Main version,
		MongoDumpArguments mongoDumpArguments,
		MongoRestoreArguments mongoRestoreArguments,
    Consumer<ServerAddress> beforeDump,
    Consumer<ServerAddress> beforeRestore,
    Consumer<ServerAddress> afterRestore
	) throws UnknownHostException {

		try (ProgressListeners.RemoveProgressListener ignored = ProgressListeners.setProgressListener(new StandardConsoleProgressListener())) {
			Transitions transitions = Defaults.transitionsForMongoRestore(version)
				.replace(Start.to(MongoRestoreArguments.class).initializedWith(mongoRestoreArguments))
				.addAll(Defaults.transitionsForMongoDump(version)
					.replace(Start.to(MongoDumpArguments.class).initializedWith(mongoDumpArguments))
					.walker().asTransitionTo(TransitionMapping.builder("mongoDump", StateID.of(RunningMongoDumpProcess.class))
						.build()))
				.addAll(Derive.given(RunningMongodProcess.class).state(ServerAddress.class)
					.deriveBy(Try.function(RunningMongodProcess::getServerAddress).mapCheckedException(RuntimeException::new)::apply))
				.addAll(Defaults.transitionsForMongod(version).walker()
					.asTransitionTo(TransitionMapping.builder("mongod", StateID.of(RunningMongodProcess.class))
						.build()));

			try (TransitionWalker.ReachedState<RunningMongodProcess> runningMongoD = transitions.walker()
				.initState(StateID.of(RunningMongodProcess.class))) {

         ServerAddress serverAddress = runningMongoD.current().getServerAddress();

         beforeDump.accept(serverAddress);

				try (TransitionWalker.ReachedState<RunningMongoDumpProcess> runningDump = runningMongoD.initState(
					StateID.of(RunningMongoDumpProcess.class))) {

					System.out.println("-------------------");
					System.out.println("dump started");
					System.out.println("-------------------");
				}

        beforeRestore.accept(serverAddress);

				try (TransitionWalker.ReachedState<RunningMongoRestoreProcess> runningDump = runningMongoD.initState(
					StateID.of(RunningMongoRestoreProcess.class))) {

					System.out.println("-------------------");
					System.out.println("restore started");
					System.out.println("-------------------");
				}

				afterRestore.accept(serverAddress);
			}
		}
	}

  private static Consumer<ServerAddress> onTestCollection(Consumer<MongoCollection<Document>> onCollection) {
     return serverAddress -> {
        try (MongoClient mongo = new MongoClient(serverAddress)) {
           MongoDatabase db = mongo.getDatabase("testdb");
           MongoCollection<Document> col = db.getCollection("testcol");

					 onCollection.accept(col);
        }
     };
  }

  @Test
	public void dumpAndRestoreFromDirectory(@TempDir Path temp) throws UnknownHostException {
		Version.Main version = Version.Main.PRODUCTION;
		Path directory = temp.resolve("dump");

		ImmutableMongoDumpArguments mongoDumpArguments = MongoDumpArguments.builder()
			.verbose(true)
			.databaseName("testdb")
			.collectionName("testcol")
			.dir(directory.toAbsolutePath().toString())
			.build();

		ImmutableMongoRestoreArguments mongoRestoreArguments = MongoRestoreArguments.builder()
			.verbose(true)
			.dir(directory.toAbsolutePath().toString())
			.build();

		String name= UUID.randomUUID().toString();

		dumpAndRestore(
      version,
      mongoDumpArguments,
      mongoRestoreArguments,
      onTestCollection(col -> col.insertOne(new Document(ImmutableMap.of("name",name)))),
      onTestCollection(col -> {
				assertThat(col.countDocuments()).isEqualTo(1);
				col.deleteMany(Document.parse("{}"));
				assertThat(col.countDocuments()).isEqualTo(0);
			}),
      onTestCollection(col -> {
				FindIterable<Document> documents = col.find(Document.parse("{}"));
				String docName = documents.map(doc -> doc.get("name", String.class)).first();
				assertThat(docName).isEqualTo(name);

				assertThat(col.countDocuments()).isEqualTo(1);
      }));
	}

	@Test
	public void dumpAndRestoreFromArchive(@TempDir Path temp) throws UnknownHostException {
		Version.Main version = Version.Main.PRODUCTION;
		Path archive = temp.resolve("archive.gz");

		ImmutableMongoDumpArguments mongoDumpArguments = MongoDumpArguments.builder()
			.verbose(true)
			.databaseName("testdb")
			.collectionName("testcol")
			.archive(archive.toAbsolutePath().toString())
			.build();

		ImmutableMongoRestoreArguments mongoRestoreArguments = MongoRestoreArguments.builder()
			.verbose(true)
			.archive(archive.toAbsolutePath().toString())
			.build();

		String name= UUID.randomUUID().toString();

		dumpAndRestore(
			version,
			mongoDumpArguments,
			mongoRestoreArguments,
			onTestCollection(col -> col.insertOne(new Document(ImmutableMap.of("name",name)))),
			onTestCollection(col -> {
				assertThat(col.countDocuments()).isEqualTo(1);
				col.deleteMany(Document.parse("{}"));
				assertThat(col.countDocuments()).isEqualTo(0);
			}),
			onTestCollection(col -> {
				FindIterable<Document> documents = col.find(Document.parse("{}"));
				String docName = documents.map(doc -> doc.get("name", String.class)).first();
				assertThat(docName).isEqualTo(name);

				assertThat(col.countDocuments()).isEqualTo(1);
			}));
	}

	@Test
	public void restoreDump() throws UnknownHostException {
		final String dumpLocation = Thread.currentThread().getContextClassLoader().getResource("dump").getFile();

		Version.Main version = Version.Main.PRODUCTION;
		ImmutableMongoRestoreArguments mongoRestoreArguments = MongoRestoreArguments.builder()
			.verbose(true)
			.dropCollection(true)
			.dir(dumpLocation)
			.build();

		try (ProgressListeners.RemoveProgressListener ignored = ProgressListeners.setProgressListener(new StandardConsoleProgressListener())) {
			Transitions transitions = Defaults.transitionsForMongoRestore(version)
				.replace(Start.to(MongoRestoreArguments.class).initializedWith(mongoRestoreArguments))
				.addAll(Derive.given(RunningMongodProcess.class).state(ServerAddress.class)
					.deriveBy(Try.function(RunningMongodProcess::getServerAddress).mapCheckedException(RuntimeException::new)::apply))
				.addAll(Defaults.transitionsForMongod(version).walker()
					.asTransitionTo(TransitionMapping.builder("mongod", StateID.of(RunningMongodProcess.class))
						.build()));

			try (TransitionWalker.ReachedState<RunningMongodProcess> runningMongoD = transitions.walker()
				.initState(StateID.of(RunningMongodProcess.class))) {

				try (TransitionWalker.ReachedState<RunningMongoRestoreProcess> runningRestore = runningMongoD.initState(
					StateID.of(RunningMongoRestoreProcess.class))) {

					System.out.println("-------------------");
					System.out.println("restore started");
					System.out.println("-------------------");
				}

				try (MongoClient mongo = new MongoClient(runningMongoD.current().getServerAddress())) {
					MongoDatabase db = mongo.getDatabase("restoredb");
					MongoCollection<Document> col = db.getCollection("sample");

					ArrayList<Object> names = Lists.newArrayList(col.find().map(doc -> doc.get("name")));

					assertThat(names).containsExactlyInAnyOrder("Cassandra", "HBase", "MongoDB");
				}
			}
		}
	}

	@Test
	public void restoreArchiveFile() throws UnknownHostException {
		final String dumpLocation = Thread.currentThread().getContextClassLoader().getResource("dump").getFile();

		Version.Main version = Version.Main.PRODUCTION;
		ImmutableMongoRestoreArguments mongoRestoreArguments = MongoRestoreArguments.builder()
			.verbose(true)
			.dropCollection(true)
			.archive(dumpLocation + "/foo.archive.gz")
			.build();

		try (ProgressListeners.RemoveProgressListener ignored = ProgressListeners.setProgressListener(new StandardConsoleProgressListener())) {
//         Defaults.transitionsForMongoDump(version)
//           .replace(Start.to(MongoDumpStarter))
			Transitions transitions = Defaults.transitionsForMongoRestore(version)
				.replace(Start.to(MongoRestoreArguments.class).initializedWith(mongoRestoreArguments))
				.addAll(Derive.given(RunningMongodProcess.class).state(ServerAddress.class)
					.deriveBy(Try.function(RunningMongodProcess::getServerAddress).mapCheckedException(RuntimeException::new)::apply))
				.addAll(Defaults.transitionsForMongod(version).walker()
					.asTransitionTo(TransitionMapping.builder("mongod", StateID.of(RunningMongodProcess.class))
						.build()));

			try (TransitionWalker.ReachedState<RunningMongodProcess> runningMongoD = transitions.walker()
				.initState(StateID.of(RunningMongodProcess.class))) {

				try (TransitionWalker.ReachedState<RunningMongoRestoreProcess> runningRestore = runningMongoD.initState(
					StateID.of(RunningMongoRestoreProcess.class))) {

					System.out.println("-------------------");
					System.out.println("restore started");
					System.out.println("-------------------");
				}

				try (MongoClient mongo = new MongoClient(runningMongoD.current().getServerAddress())) {
					MongoDatabase db = mongo.getDatabase("restoredb");
					MongoCollection<Document> col = db.getCollection("sample");

					ArrayList<Object> names = Lists.newArrayList(col.find().map(doc -> doc.get("name")));

					assertThat(names).containsExactlyInAnyOrder("Cassandra", "HBase", "MongoDB");
				}
			}
		}
	}

	@Test
	public void testStartMongoRestore() throws IOException, InterruptedException {

		final int serverPort = Network.getFreeServerPort();
		final String dumpLocation = Thread.currentThread().getContextClassLoader().getResource("dump").getFile();

		final Version.Main version = Version.Main.PRODUCTION;
		final MongodConfig mongodConfig = MongodConfig.builder()
			.version(version)
			.net(new Net(serverPort, Network.localhostIsIPv6()))
			.cmdOptions(getCmdOptions(version))
			.build();

		final RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(Command.MongoD).build();

		final MongodExecutable mongodExe = MongodStarter.getInstance(runtimeConfig).prepare(mongodConfig);
		final MongodProcess mongod = mongodExe.start();

		final MongoRestoreExecutable mongoRestoreExecutable = mongoRestoreExecutable(serverPort, dumpLocation, true);
		final MongoRestoreExecutable mongoRestoreExecutableArchive = mongoRestoreExecutableWithArchiveCompressed(serverPort, dumpLocation, true);

		MongoRestoreProcess mongoRestoreProcess = null;
		MongoRestoreProcess mongoRestoreArchiveProcess = null;

		boolean dataRestored = false;
		try {
			mongoRestoreProcess = mongoRestoreExecutable.start();
			mongoRestoreArchiveProcess = mongoRestoreExecutableArchive.start();

			dataRestored = true;

		}
		catch (Exception e) {
			_logger.info("MongoRestore exception: {}", e.getStackTrace());
			dataRestored = false;
		}
		finally {
			Assertions.assertThat(dataRestored)
				.describedAs("mongoDB restore data in json format")
				.isTrue();
			mongoRestoreProcess.stop();
			mongoRestoreArchiveProcess.stop();
		}

		mongod.stop();
		mongodExe.stop();
	}

	private MongoRestoreExecutable mongoRestoreExecutable(final int port,
		final String dumpLocation,
		final Boolean drop) throws IOException {

		MongoRestoreConfig mongoRestoreConfig = MongoRestoreConfig.builder()
			.version(Version.Main.PRODUCTION)
			.net(new Net(port, Network.localhostIsIPv6()))
			.isDropCollection(drop)
			.dir(dumpLocation)
			.build();

		return MongoRestoreStarter.getDefaultInstance().prepare(mongoRestoreConfig);
	}

	private MongoRestoreExecutable mongoRestoreExecutableWithArchiveCompressed(final int port,
		final String dumpLocation,
		final Boolean drop) throws IOException {

		MongoRestoreConfig mongoRestoreConfig = MongoRestoreConfig.builder()
			.version(Version.Main.PRODUCTION)
			.archive(String.format("%s/%s", dumpLocation, _archiveFileCompressed))
			.isGzip(true)
			.net(new Net(port, Network.localhostIsIPv6()))
			.isDropCollection(drop)
			.build();

		return MongoRestoreStarter.getDefaultInstance().prepare(mongoRestoreConfig);
	}
}
