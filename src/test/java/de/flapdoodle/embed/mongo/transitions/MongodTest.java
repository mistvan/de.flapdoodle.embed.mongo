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
package de.flapdoodle.embed.mongo.transitions;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.Versions;
import de.flapdoodle.embed.mongo.commands.MongodArguments;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.packageresolver.Feature;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.runtime.Network;
import de.flapdoodle.os.*;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.transitions.Start;
import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.junit.Assume;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertNotNull;

class MongodTest {
	private static final Logger logger = LoggerFactory.getLogger(MongodTest.class.getName());

	@Test
	public void testStartStopTenTimesWithNewMongoExecutable() throws IOException {
		int loops = 10;

		Mongod mongod = new Mongod() {
			@Override public Transition<MongodArguments> mongodArguments() {
				return Start.to(MongodArguments.class)
					.initializedWith(MongodArguments.defaults()
						.withUseNoPrealloc(true)
						.withUseSmallFiles(true));
			}
		};

		for (int i = 0; i < loops; i++) {
			logger.info("Loop: {}", i);
			try (TransitionWalker.ReachedState<RunningMongodProcess> runningMongod = mongod.start(Version.Main.PRODUCTION)) {
				try (MongoClient mongo = new MongoClient(runningMongod.current().getServerAddress())) {
					DB db = mongo.getDB("test");
					DBCollection col = db.createCollection("testCol", new BasicDBObject());
					col.save(new BasicDBObject("testDoc", new Date()));
				}
			}
		}
	}

	@Test
	public void startTwoMongodInstancesUsingDifferentPorts() throws UnknownHostException {
		try (TransitionWalker.ReachedState<RunningMongodProcess> outerMongod = Mongod.instance().start(Version.Main.PRODUCTION)) {
			try (TransitionWalker.ReachedState<RunningMongodProcess> innerMongod = Mongod.instance().start(Version.Main.PRODUCTION)) {

				try (MongoClient mongo = new MongoClient(innerMongod.current().getServerAddress())) {
					MongoDatabase db = mongo.getDatabase("test");
					db.createCollection("testCol");
					MongoCollection<Document> col = db.getCollection("testColl");
					col.insertOne(new Document("testDoc", new Date()));
				}

				try (MongoClient mongo = new MongoClient(outerMongod.current().getServerAddress())) {
					MongoDatabase db = mongo.getDatabase("test");
					db.createCollection("testCol");
					MongoCollection<Document> col = db.getCollection("testColl");
					col.insertOne(new Document("testDoc", new Date()));
				}
			}
		}
	}

	@Test
	public void testStartMongodOnNonFreePort() {
		Net net = Net.defaults();

		Mongod mongod = new Mongod() {
			@Override public Transition<Net> net() {
				return Start.to(Net.class)
					.initializedWith(net);
			}
		};

		try (TransitionWalker.ReachedState<RunningMongodProcess> outerMongod = mongod.start(Version.Main.PRODUCTION)) {
			Assertions.assertThatThrownBy(() -> mongod.start(Version.Main.PRODUCTION))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("error on transition to State(de.flapdoodle.embed.mongo.transitions.RunningMongodProcess), rollback");
		}
	}

	@Test
	public void startLegacyMongodWichDontSupportBindIpArgument() throws IOException {
		Net net = Net.of("localhost",
			Network.freeServerPort(Network.getLocalHost()),
			Network.localhostIsIPv6());

		try (TransitionWalker.ReachedState<RunningMongodProcess> running = new Mongod() {
			@Override
			public Transition<Net> net() {
				return Start.to(Net.class).initializedWith(net);
			}
		}.start(Version.V3_6_0)) {
			try (MongoClient mongo = new MongoClient(running.current().getServerAddress())) {
				MongoDatabase db = mongo.getDatabase("test");
				MongoCollection<Document> col = db.getCollection("testCol");
				col.insertOne(new Document("testDoc", new Date()));
			}
		}
	}

	@Test
	public void shutdownShouldWorkWithMongodbVersion6() throws UnknownHostException {
		Version.Main version = Version.Main.V6_0;

		try (TransitionWalker.ReachedState<RunningMongodProcess> outerMongod = Mongod.instance()
			.withNet(Start.to(Net.class).initializedWith(Net.of("localhost", 23456, Network.localhostIsIPv6())))
			.start(version)) {
			try (MongoClient mongo = new MongoClient(outerMongod.current().getServerAddress())) {
				MongoDatabase db = mongo.getDatabase("test");
				MongoCollection<Document> col = db.getCollection("testCol");
				col.insertOne(new Document("testDoc", new Date()));

				MongoDatabase adminDB = mongo.getDatabase("admin");
				System.out.println(outerMongod.current().getServerAddress());

				if (version== Version.Main.V6_0 && false) {
					try {
						Document result = adminDB.runCommand(new Document()
							.append("shutdown", 1)
//							.append("force", true)
							//.append("comment","-----------------------------------------------------------------------------------------------------------")
						);
					}
					catch (Exception x) {
						x.printStackTrace();
					}
				} else {
					System.out.println("skip");
				}


//				MongoDatabase db = mongo.getDatabase("test");
//				db.createCollection("testCol");
//				MongoCollection<Document> col = db.getCollection("testColl");
//				col.insertOne(new Document("testDoc", new Date()));
			}
		}
	}

	private static List<Arguments> testableDistributions() {
		ArrayList<Arguments> ret = new ArrayList<>();
		for (OS os : OS.values()) {
			// there is no generic linux version of mongodb after 3.6
			// so we should use linux dists instead
			for (CommonArchitecture arc : CommonArchitecture.values()) {
				for (Version.Main version : Versions.testableVersions(Version.Main.class)) {
					ret.add(Arguments.of(os,arc,version));
				}
			}
		}
		return ret;
	}

	@ParameterizedTest
	@MethodSource("testableDistributions")
	public void extractArtifact(OS os, CommonArchitecture arch, Version.Main version) {
		if (skipThisVersion(os, version, arch.bitSize())) {
			Assume.assumeTrue(true);
		} else {
			assertCanExtractArtifact(distributionOf(version, os, arch));
		}
	}

	private static void assertCanExtractArtifact(Distribution distribution) {
		try (TransitionWalker.ReachedState<de.flapdoodle.embed.process.archives.ExtractedFileSet> extractedFileSet = new Mongod() {
			@Override
			public Transition<Distribution> distribution() {
				return Start.to(Distribution.class).initializedWith(distribution);
			}
		}.transitions(distribution.version())
			.walker()
			.initState(StateID.of(de.flapdoodle.embed.process.archives.ExtractedFileSet.class))) {
			assertNotNull(extractedFileSet.current().executable());
		}
	}

	private static boolean skipThisVersion(OS os, IFeatureAwareVersion version, BitSize bitsize) {
		if (version.enabled(Feature.ONLY_64BIT) && bitsize==BitSize.B32) {
			return true;
		}

		if ((os == OS.OS_X) && (bitsize == BitSize.B32)) {
			// there is no osx 32bit version for v2.2.1 and above, so we dont check
			return true;
		}
		if ((os == OS.Solaris)  && ((bitsize == BitSize.B32) || version.enabled(Feature.NO_SOLARIS_SUPPORT))) {
			return true;
		}
		if (os == OS.FreeBSD) {
			return true;
		}
		if (os == OS.Windows) {
			// there is no windows 2008 version for 1.8.5
			return version.asInDownloadPath().equals(Version.V1_8_5.asInDownloadPath());
		}
		return false;
	}

	private static Distribution distributionOf(IFeatureAwareVersion version, OS os, Architecture architecture) {
		return Distribution.of(version, ImmutablePlatform.builder()
			.operatingSystem(os)
			.architecture(architecture)
			.build());
	}
}