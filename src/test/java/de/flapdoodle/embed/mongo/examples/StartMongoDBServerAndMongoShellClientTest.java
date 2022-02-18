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

import com.google.common.collect.Lists;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import de.flapdoodle.embed.mongo.*;
import de.flapdoodle.embed.mongo.commands.MongoDumpArguments;
import de.flapdoodle.embed.mongo.commands.MongoShellArguments;
import de.flapdoodle.embed.mongo.config.Defaults;
import de.flapdoodle.embed.mongo.config.MongoShellConfig;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.ExecutedMongoDumpProcess;
import de.flapdoodle.embed.mongo.transitions.ExecutedMongoShellProcess;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;

import java.io.IOException;
import java.net.UnknownHostException;

import static de.flapdoodle.embed.mongo.TestUtils.getCmdOptions;


public class StartMongoDBServerAndMongoShellClientTest {

	@org.junit.jupiter.api.Test
	public void startMongoShell() {
		Version.Main version= Version.Main.PRODUCTION;
		MongoShellArguments mongoShellArguments=MongoShellArguments.builder()
			.addScriptParameters("var hight=3","var width=2","function multip() { print('area ' + hight * width); }","multip()")
			.build();

		try (ProgressListeners.RemoveProgressListener ignored = ProgressListeners.setProgressListener(new StandardConsoleProgressListener())) {
			Transitions transitions = Defaults.transitionsForMongoShell(version)
				.replace(Start.to(MongoShellArguments.class).initializedWith(mongoShellArguments))
				.addAll(Derive.given(RunningMongodProcess.class).state(ServerAddress.class)
					.deriveBy(Try.function(RunningMongodProcess::getServerAddress).mapCheckedException(RuntimeException::new)::apply))
				.addAll(Defaults.transitionsForMongod(version).walker()
					.asTransitionTo(TransitionMapping.builder("mongod", StateID.of(RunningMongodProcess.class))
						.build()));

			try (TransitionWalker.ReachedState<RunningMongodProcess> runningMongoD = transitions.walker()
				.initState(StateID.of(RunningMongodProcess.class))) {

				try (TransitionWalker.ReachedState<ExecutedMongoShellProcess> executedDump = runningMongoD.initState(
					StateID.of(ExecutedMongoShellProcess.class))) {

					System.out.println("-------------------");
					System.out.println("shell executed: "+executedDump.current().returnCode());
					System.out.println("-------------------");
				}
			}
		}
	}


	/*
	 // ->
	 this is an very easy example to use mongos and mongod
	 // <- 
	 */
//	@Test
	public void startAndStopMongoDBAndMongoShell() throws IOException {
			// ->
		int port = Network.getFreeServerPort();
		String defaultHost = "localhost";

		MongodProcess mongod = startMongod(port);

		try {
			Thread.sleep(1000);
			MongoShellProcess mongoShell = startMongoShell(port, defaultHost);
			Thread.sleep(1000);
			try {
				MongoClient mongoClient = new MongoClient(defaultHost, port);
				System.out.println("DB Names: " + mongoClient.getDatabaseNames());
			} finally {
				mongoShell.stop();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mongod.stop();
		}
			// <-
	}
	
	// ->
	private MongoShellProcess startMongoShell(int defaultConfigPort, String defaultHost) throws UnknownHostException,
			IOException {
		MongoShellConfig mongoShellConfig = MongoShellConfig.builder()
			.version(Version.Main.PRODUCTION)
			.net(new Net(defaultConfigPort, Network.localhostIsIPv6()))
			.scriptParameters(Lists.newArrayList("var hight=3","var width=2","function multip() { print('area ' + hight * width); }","multip()"))
			.build();

		MongoShellExecutable mongosExecutable = MongoShellStarter.getDefaultInstance().prepare(mongoShellConfig);
		MongoShellProcess mongos = mongosExecutable.start();
		return mongos;
	}

	private MongodProcess startMongod(int defaultConfigPort) throws IOException {
		final Version.Main version = Version.Main.PRODUCTION;
		MongodConfig mongoConfigConfig = MongodConfig.builder()
			.version(version)
			.cmdOptions(getCmdOptions(version))
			.net(new Net(defaultConfigPort, Network.localhostIsIPv6()))
			.build();

		MongodExecutable mongodExecutable = MongodStarter.getDefaultInstance().prepare(mongoConfigConfig);
		MongodProcess mongod = mongodExecutable.start();
		return mongod;
	}
	// <-
}
