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

import java.io.IOException;

import org.bson.Document;
import org.junit.Ignore;
import org.junit.Test;

import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.MongosExecutable;
import de.flapdoodle.embed.mongo.MongosProcess;
import de.flapdoodle.embed.mongo.MongosStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.MongosConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.Storage;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;


public class StartConfigAndMongoDBServerTest {

	/*
	 this is an very easy example to use mongos and mongod
	 */
	@Test
	@Ignore
	public void startAndStopMongosAndMongod() throws IOException {
		int mongosPort = Network.getFreeServerPort();
		int mongodPort = Network.getFreeServerPort();
		String defaultHost = "localhost";

		MongodProcess mongod = startMongod(mongodPort);

		try {
			// init replica set, aka rs.initiate()
			try (MongoClient client=new MongoClient(defaultHost, mongodPort)) {
				client.getDatabase("admin").runCommand(new Document("replSetInitiate", new Document()));
			}
			
			MongosProcess mongos = startMongos(mongosPort, mongodPort, defaultHost);
			
			try {
				try (MongoClient mongoClient = new MongoClient(defaultHost, mongodPort)) {
					System.out.println("DB Names: " + mongoClient.getDatabaseNames());
				}
			} finally {
				mongos.stop();
			}
		} finally {
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
