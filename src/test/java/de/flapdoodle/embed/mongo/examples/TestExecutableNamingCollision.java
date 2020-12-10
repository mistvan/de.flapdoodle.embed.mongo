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

import org.junit.Test;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.Defaults;
import de.flapdoodle.embed.mongo.config.ImmutableMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import de.flapdoodle.embed.process.extract.UserTempNaming;
import de.flapdoodle.embed.process.io.directories.PropertyOrPlatformTempDir;
import de.flapdoodle.embed.process.runtime.Network;
import de.flapdoodle.embed.process.store.ArtifactStore;
import de.flapdoodle.embed.process.store.UrlConnectionDownloader;
import junit.framework.TestCase;

public class TestExecutableNamingCollision extends TestCase {

	private MongodExecutable _mongodExe;
	private MongodProcess _mongod;

	private MongoClient _mongo;
	
	private static final RuntimeConfig _runtimeConfig=runtimeConfig();

	private static RuntimeConfig runtimeConfig() {
		return Defaults.runtimeConfigFor(Command.MongoD)
		.artifactStore(ArtifactStore.builder()
		.tempDirFactory(new PropertyOrPlatformTempDir())
		.executableNaming(new UUIDTempNaming())
		.downloadConfig(Defaults.downloadConfigFor(Command.MongoD).build())
		.downloader(new UrlConnectionDownloader())
				.executableNaming(new UserTempNaming())
				.build()
				.withCache())
		.build();
	}
	
	
	@Override
	protected void setUp() throws Exception {
		MongodStarter runtime = MongodStarter.getInstance(_runtimeConfig);
		_mongodExe = runtime.prepare(createMongodConfig());
		_mongod = _mongodExe.start();

		super.setUp();

		_mongo = new MongoClient("localhost", 12345);
	}

	protected MongodConfig createMongodConfig() throws UnknownHostException, IOException {
		return createMongodConfigBuilder().build();
	}

	protected ImmutableMongodConfig.Builder createMongodConfigBuilder() throws UnknownHostException, IOException {
		return MongodConfig.builder().version(Version.Main.PRODUCTION).net(new Net(12345, Network.localhostIsIPv6()));
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

	@Test
	public void testOne() {
		System.out.println("------------------------One-----------------------------");
	}

	@Test
	public void testTwo() {
		System.out.println("------------------------Two-----------------------------");
	}

	@Test
	public void testAnOther() {
		System.out.println("------------------------3-----------------------------");
	}
}
