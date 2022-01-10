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

import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.*;
import de.flapdoodle.embed.mongo.commands.MongodArguments;
import de.flapdoodle.embed.mongo.commands.MongosArguments;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.embed.mongo.transitions.RunningMongosProcess;
import de.flapdoodle.embed.process.runtime.Network;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.TransitionMapping;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.Transitions;
import de.flapdoodle.reverse.transitions.Start;
import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.UnknownHostException;

import static org.assertj.core.api.Assertions.assertThat;

public class StartConfigAndMongoDBServerTest {

	@Test
	public void mongosAndMongod() throws UnknownHostException {
		Version.Main version = Version.Main.PRODUCTION;

		try (TransitionWalker.ReachedState<RunningMongodProcess> runningMongod = Defaults.transitionsForMongod(version)
			.replace(Start.to(MongodArguments.class).initializedWith(MongodArguments.defaults()
				.withIsConfigServer(true)
				.withReplication(new Storage(null, "testRepSet", 5000))))
			.walker()
			.initState(StateID.of(RunningMongodProcess.class))) {

			try (MongoClient mongo = new MongoClient(runningMongod.current().getServerAddress())) {
				mongo.getDatabase("admin").runCommand(new Document("replSetInitiate", new Document()));
			}

			try (TransitionWalker.ReachedState<RunningMongosProcess> runningMongos = Defaults.transitionsForMongos(version)
				.replace(Start.to(MongosArguments.class).initializedWith(MongosArguments.defaults()
					.withConfigDB(runningMongod.current().getServerAddress().toString())
					.withReplicaSet("testRepSet")
				))

				.walker()
				.initState(StateID.of(RunningMongosProcess.class))) {

				try (MongoClient mongo = new MongoClient(runningMongod.current().getServerAddress())) {
					assertThat(mongo.listDatabaseNames()).contains("admin","config","local");
				}

			}
		}
	}

	/*
	 this is an very easy example to use mongos and mongod
	 */
	@Test
	@Disabled
	public void startAndStopMongosAndMongod() throws IOException {
		int mongosPort = Network.getFreeServerPort();
		int mongodPort = Network.getFreeServerPort();
		String defaultHost = "localhost";

		MongodProcess mongod = startMongod(mongodPort);

		try {
			// init replica set, aka rs.initiate()
			try (MongoClient client = new MongoClient(defaultHost, mongodPort)) {
				client.getDatabase("admin").runCommand(new Document("replSetInitiate", new Document()));
			}

			MongosProcess mongos = startMongos(mongosPort, mongodPort, defaultHost);

			try {
				try (MongoClient mongoClient = new MongoClient(defaultHost, mongodPort)) {
					System.out.println("DB Names: " + mongoClient.getDatabaseNames());
				}
			}
			finally {
				mongos.stop();
			}
		}
		finally {
			mongod.stop();
		}
	}

	private MongosProcess startMongos(int port, int defaultConfigPort, String defaultHost) throws
		IOException {
		MongosConfig mongosConfig = MongosConfig.builder()
			.version(Version.Main.PRODUCTION)
			.net(new Net(port, Network.localhostIsIPv6()))
			.configDB(defaultHost + ":" + defaultConfigPort)
			.replicaSet("testRepSet")
			.build();

		MongosExecutable mongosExecutable = MongosStarter.getDefaultInstance().prepare(mongosConfig);
		return mongosExecutable.start();
	}

	private MongodProcess startMongod(int defaultConfigPort) throws IOException {
		MongodConfig mongoConfigConfig = MongodConfig.builder()
			.version(Version.Main.PRODUCTION)
			.net(new Net(defaultConfigPort, Network.localhostIsIPv6()))
			.replication(new Storage(null, "testRepSet", 5000))
			.isConfigServer(true)
			.build();

		MongodExecutable mongodExecutable = MongodStarter.getDefaultInstance().prepare(mongoConfigConfig);
		return mongodExecutable.start();
	}
}
