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

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.commands.ImmutableMongoImportArguments;
import de.flapdoodle.embed.mongo.commands.MongoImportArguments;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.ExecutedMongoImportProcess;
import de.flapdoodle.embed.mongo.transitions.MongoImport;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.TransitionMapping;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.Transitions;
import de.flapdoodle.reverse.transitions.Derive;
import de.flapdoodle.reverse.transitions.Start;
import de.flapdoodle.types.Try;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class HowToUseTransitionsTest {

	@Test
	public void startMongoD() throws UnknownHostException {
		Transitions transitions = Mongod.instance().transitions(Version.Main.PRODUCTION);

		String dot = Transitions.edgeGraphAsDot("mongod", transitions.asGraph());
		System.out.println("---------------------");
		System.out.println(dot);
		System.out.println("---------------------");

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

		Version.Main version = Version.Main.PRODUCTION;

		try (TransitionWalker.ReachedState<RunningMongodProcess> mongoD = Mongod.instance().transitions(version)
			.walker()
			.initState(StateID.of(RunningMongodProcess.class))) {

			Transitions transitions = MongoImport.instance().transitions(version)
				.replace(Start.to(MongoImportArguments.class).initializedWith(arguments));

			String dot = Transitions.edgeGraphAsDot("mongoImport", transitions.asGraph());
			System.out.println("---------------------");
			System.out.println(dot);
			System.out.println("---------------------");

			Transitions withMongoDbServerAddress = transitions.addAll(Start.to(ServerAddress.class).initializedWith(mongoD.current().getServerAddress()));

			try (TransitionWalker.ReachedState<ExecutedMongoImportProcess> executed = withMongoDbServerAddress.walker()
				.initState(StateID.of(ExecutedMongoImportProcess.class))) {

				assertThat(executed.current().returnCode())
					.describedAs("mongo import was successful")
					.isEqualTo(0);
			}

			try (MongoClient mongo = new MongoClient(mongoD.current().getServerAddress())) {
				MongoDatabase db = mongo.getDatabase("importDatabase");
				MongoCollection<Document> col = db.getCollection("importCollection");

				ArrayList<Object> names = Lists.newArrayList(col.find().map(doc -> doc.get("name")));

				assertThat(names).containsExactlyInAnyOrder("Cassandra","HBase","MongoDB");
			}
		}
	}

	@Test
	public void startMongoImportAsOneTransition() throws UnknownHostException {
		ImmutableMongoImportArguments arguments = MongoImportArguments.builder()
			.databaseName("importDatabase")
			.collectionName("importCollection")
			.importFile(Resources.getResource("sample.json").getFile())
			.isJsonArray(true)
			.upsertDocuments(true)
			.build();

		Version.Main version = Version.Main.PRODUCTION;

		Transitions mongoImportTransitions = MongoImport.instance().transitions(version)
			.replace(Start.to(MongoImportArguments.class).initializedWith(arguments))
			.addAll(Derive.given(RunningMongodProcess.class).state(ServerAddress.class)
				.deriveBy(Try.function(RunningMongodProcess::getServerAddress).mapCheckedException(RuntimeException::new)::apply))
			.addAll(Mongod.instance().transitions(version).walker()
				.asTransitionTo(TransitionMapping.builder("mongod", StateID.of(RunningMongodProcess.class))
					.build()));


		String dot = Transitions.edgeGraphAsDot("mongoImport", mongoImportTransitions.asGraph());
		System.out.println("---------------------");
		System.out.println(dot);
		System.out.println("---------------------");

		try (TransitionWalker.ReachedState<RunningMongodProcess> mongoD = mongoImportTransitions.walker()
			.initState(StateID.of(RunningMongodProcess.class))) {

			try (TransitionWalker.ReachedState<ExecutedMongoImportProcess> running = mongoD.initState(StateID.of(ExecutedMongoImportProcess.class))) {
				System.out.println("import done: "+running.current().returnCode());

				assertThat(running.current().returnCode())
					.describedAs("import successful")
					.isEqualTo(0);
			}

			try (MongoClient mongo = new MongoClient(mongoD.current().getServerAddress())) {
				MongoDatabase db = mongo.getDatabase("importDatabase");
				MongoCollection<Document> col = db.getCollection("importCollection");

				ArrayList<Object> names = Lists.newArrayList(col.find().map(doc -> doc.get("name")));

				assertThat(names).containsExactlyInAnyOrder("Cassandra","HBase","MongoDB");
			}
		}
	}
}
