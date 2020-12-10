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
import java.net.UnknownHostException;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.ImmutableMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import junit.framework.TestCase;

// ->
public abstract class AbstractMongoDBTest extends TestCase {


	/**
	 * please store Starter or RuntimeConfig in a static final field
	 * if you want to use artifact store caching (or else disable caching) 
	 */
	private static final MongodStarter starter = MongodStarter.getDefaultInstance();

	private MongodExecutable _mongodExe;
	private MongodProcess _mongod;

	private MongoClient _mongo;
	private int port;
	
	@Override
	protected void setUp() throws Exception {
		port = Network.getFreeServerPort();
		_mongodExe = starter.prepare(createMongodConfig());
		_mongod = _mongodExe.start();

		super.setUp();

		_mongo = new MongoClient("localhost", port);
	}
	
	public int port() {
		return port;
	}

	protected MongodConfig createMongodConfig() throws UnknownHostException, IOException {
		return createMongodConfigBuilder().build();
	}

	protected ImmutableMongodConfig.Builder createMongodConfigBuilder() throws UnknownHostException, IOException {
		return MongodConfig.builder()
			.version(Version.Main.PRODUCTION)
			.net(new Net(port, Network.localhostIsIPv6()));
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		_mongod.stop();
		_mongodExe.stop();
	}

	public Mongo getMongo() {
		return _mongo;
	}

}
// <-
