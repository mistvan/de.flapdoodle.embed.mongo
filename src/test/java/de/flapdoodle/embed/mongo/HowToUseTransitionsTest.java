package de.flapdoodle.embed.mongo;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.commands.ImmutableMongoImportArguments;
import de.flapdoodle.embed.mongo.commands.MongoImportArguments;
import de.flapdoodle.embed.mongo.config.Defaults;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.RunningMongoImportProcess;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.embed.process.io.progress.ProgressListeners;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.TransitionMapping;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.Transitions;
import de.flapdoodle.reverse.transitions.Derive;
import de.flapdoodle.reverse.transitions.Start;
import de.flapdoodle.types.Try;
import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class HowToUseTransitionsTest {

	@Test
	public void startMongoD() throws UnknownHostException {
		Transitions transitions = Defaults.transitionsForMongod(Version.Main.PRODUCTION);

		String dot = Transitions.edgeGraphAsDot("mongod", transitions.asGraph());
		System.out.println("---------------------");
		System.out.println(dot);
		System.out.println("---------------------");

		try (ProgressListeners.RemoveProgressListener ignored = ProgressListeners.setProgressListener(new StandardConsoleProgressListener())) {

			for (int i = 0; i < 2; i++) {
				try (TransitionWalker.ReachedState<RunningMongodProcess> running = transitions.walker()
					.initState(StateID.of(RunningMongodProcess.class))) {

					try (MongoClient mongo = new MongoClient(running.current().getServerAddress())) {
						MongoDatabase db = mongo.getDatabase("test");
						MongoCollection<Document> col = db.getCollection("testCol");
						col.insertOne(new Document("testDoc", new Date()));
						System.out.println("could store doc in database...");
					}
				}
			}
		}
	}

	@Test
	public void startMongoImport() throws UnknownHostException {

		File jsonFile = new File(Thread.currentThread().getContextClassLoader().getResource("sample.json").getFile());

		ImmutableMongoImportArguments arguments = MongoImportArguments.builder()
			.databaseName("importDatabase")
			.collectionName("importCollection")
			.importFile(jsonFile.getAbsolutePath())
			.isJsonArray(true)
			.upsertDocuments(true)
			.build();

		Transitions transitions = Defaults.transitionsForMongoImport(Version.Main.PRODUCTION)
			.replace(Start.to(MongoImportArguments.class).initializedWith(arguments));

		String dot = Transitions.edgeGraphAsDot("mongoImport", transitions.asGraph());
		System.out.println("---------------------");
		System.out.println(dot);
		System.out.println("---------------------");

		try (ProgressListeners.RemoveProgressListener ignored = ProgressListeners.setProgressListener(new StandardConsoleProgressListener())) {

			try (TransitionWalker.ReachedState<RunningMongodProcess> mongoD = Defaults.transitionsForMongod(Version.Main.PRODUCTION).walker()
				.initState(StateID.of(RunningMongodProcess.class))) {

				Transitions withMongoDbServerAddress = transitions.addAll(Start.to(ServerAddress.class).initializedWith(mongoD.current().getServerAddress()));

				try (TransitionWalker.ReachedState<RunningMongoImportProcess> running = withMongoDbServerAddress.walker()
					.initState(StateID.of(RunningMongoImportProcess.class))) {

					try (MongoClient mongo = new MongoClient(mongoD.current().getServerAddress())) {
						MongoDatabase db = mongo.getDatabase("importDatabase");
						MongoCollection<Document> col = db.getCollection("importCollection");

						ArrayList<Object> names = Lists.newArrayList(col.find().map(doc -> doc.get("name")));

						assertThat(names).containsExactlyInAnyOrder("Cassandra","HBase","MongoDB");
					}
				}
			}
		}
	}

	@Test
	public void startMongoImportAsOneTransition() throws UnknownHostException {

		File jsonFile = new File(Thread.currentThread().getContextClassLoader().getResource("sample.json").getFile());

		ImmutableMongoImportArguments arguments = MongoImportArguments.builder()
			.databaseName("importDatabase")
			.collectionName("importCollection")
			.importFile(jsonFile.getAbsolutePath())
			.isJsonArray(true)
			.upsertDocuments(true)
			.build();

		Transitions mongodTransitions = Defaults.transitionsForMongod(Version.Main.PRODUCTION);

		Transitions mongoImportTransitions = Defaults.transitionsForMongoImport(Version.Main.PRODUCTION)
			.replace(Start.to(MongoImportArguments.class).initializedWith(arguments))
			.addAll(Derive.given(RunningMongodProcess.class).state(ServerAddress.class)
				.deriveBy(Try.function(RunningMongodProcess::getServerAddress).mapCheckedException(RuntimeException::new)::apply))
			.addAll(mongodTransitions.walker()
				.asTransitionTo(TransitionMapping.builder("mongod", StateID.of(RunningMongodProcess.class))
					.build()));


		String dot = Transitions.edgeGraphAsDot("mongoImport", mongoImportTransitions.asGraph());
		System.out.println("---------------------");
		System.out.println(dot);
		System.out.println("---------------------");

		try (ProgressListeners.RemoveProgressListener ignored = ProgressListeners.setProgressListener(new StandardConsoleProgressListener())) {

			try (TransitionWalker.ReachedState<RunningMongodProcess> mongoD = mongoImportTransitions.walker()
				.initState(StateID.of(RunningMongodProcess.class))) {

				try (TransitionWalker.ReachedState<RunningMongoImportProcess> running = mongoD.initState(StateID.of(RunningMongoImportProcess.class))) {

					try (MongoClient mongo = new MongoClient(mongoD.current().getServerAddress())) {
						MongoDatabase db = mongo.getDatabase("importDatabase");
						MongoCollection<Document> col = db.getCollection("importCollection");

						ArrayList<Object> names = Lists.newArrayList(col.find().map(doc -> doc.get("name")));

						assertThat(names).containsExactlyInAnyOrder("Cassandra","HBase","MongoDB");
					}
				}
			}
		}
	}
}
