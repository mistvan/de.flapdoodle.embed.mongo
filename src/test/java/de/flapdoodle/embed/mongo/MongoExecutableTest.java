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

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

import de.flapdoodle.embed.mongo.config.Defaults;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.runtime.Network;

/**
 * Integration test for starting and stopping MongodExecutable
 * 
 * @author m.joehren
 */
//CHECKSTYLE:OFF
public class MongoExecutableTest {

	private static final Logger logger = LoggerFactory.getLogger(MongoExecutableTest.class.getName());

	@Test
	public void testStartStopTenTimesWithNewMongoExecutable() throws IOException {
		boolean useMongodb = true;
		int loops = 10;

		MongodConfig mongodConfig = MongodConfig.builder().version(Version.Main.PRODUCTION).stopTimeoutInMillis(5L).net(new Net(Network.getFreeServerPort(), Network.localhostIsIPv6())).build();

		RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(Command.MongoD).build();

		for (int i = 0; i < loops; i++) {
			logger.info("Loop: {}", i);
			MongodExecutable mongodExe = MongodStarter.getInstance(runtimeConfig).prepare(mongodConfig);
			try {
				MongodProcess mongod = mongodExe.start();

				try (MongoClient mongo = new MongoClient(
                        new ServerAddress(mongodConfig.net().getServerAddress(), mongodConfig.net().getPort()))) {
                    DB db = mongo.getDB("test");
                    DBCollection col = db.createCollection("testCol", new BasicDBObject());
                    col.save(new BasicDBObject("testDoc", new Date()));
                }

				mongod.stop();
			} finally {
				mongodExe.stop();
			}
		}

	}

	@Test
	public void testStartMongodOnNonFreePort() throws IOException, InterruptedException {
		int port = Network.getFreeServerPort();
		
		MongodConfig mongodConfig = MongodConfig.builder().version(Version.Main.PRODUCTION).net(new Net(port, Network.localhostIsIPv6())).build();

		RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(Command.MongoD).build();

		MongodExecutable mongodExe = MongodStarter.getInstance(runtimeConfig).prepare(mongodConfig);
		MongodProcess mongod = mongodExe.start();

		boolean innerMongodCouldNotStart = false;
		{
			Thread.sleep(500);

			MongodExecutable innerExe = MongodStarter.getInstance(runtimeConfig).prepare(mongodConfig);
			try {
				MongodProcess innerMongod = innerExe.start();
				assertNotNull(innerMongod);
			} catch (RuntimeException iox) {
				innerMongodCouldNotStart = true;
			} finally {
				innerExe.stop();
				Assert.assertTrue("inner Mongod could not start", innerMongodCouldNotStart);
			}
		}

		mongod.stop();
		mongodExe.stop();
	}

}
