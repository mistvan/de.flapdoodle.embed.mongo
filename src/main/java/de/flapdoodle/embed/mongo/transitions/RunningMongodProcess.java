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

import de.flapdoodle.embed.mongo.commands.ServerAddress;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.runtime.Mongod;
import de.flapdoodle.embed.process.config.SupportConfig;
import de.flapdoodle.embed.process.io.*;
import de.flapdoodle.embed.process.runtime.ProcessControl;
import de.flapdoodle.embed.process.runtime.Processes;
import de.flapdoodle.embed.process.types.RunningProcessFactory;
import de.flapdoodle.embed.process.types.RunningProcessImpl;
import de.flapdoodle.os.Platform;
import de.flapdoodle.types.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RunningMongodProcess extends RunningProcessImpl {

	private static Logger LOGGER= LoggerFactory.getLogger(RunningMongodProcess.class);

	private final SupportConfig supportConfig;
	private final Platform platform;
	private final Net net;
	private final StreamProcessor commandOutput;
	private final int mongodProcessId;
	
	public RunningMongodProcess(
		ProcessControl process,
		Path pidFile,
		long timeout,
		Runnable onStop,
		SupportConfig supportConfig,
		Platform platform,
		Net net,
		StreamProcessor commandOutput,
		int mongodProcessId
	) {
		super(process, pidFile, timeout, onStop);
		this.supportConfig = supportConfig;
		this.platform = platform;
		this.commandOutput = commandOutput;
		this.net = net;
		this.mongodProcessId = mongodProcessId;
	}

	public ServerAddress getServerAddress() throws UnknownHostException {
		return ServerAddress.of(net.getServerAddress(), net.getPort());
	}

	@Override
	public int stop() {
		try {
			stopInternal();
		} finally {
			return super.stop();
		}
	}

	//	@Override
	private void stopInternal() {
			LOGGER.debug("try to stop mongod");
			if (!sendStopToMongoInstance()) {
				LOGGER.warn("could not stop mongod with db command, try next");
				if (!sendKillToProcess()) {
					LOGGER.warn("could not stop mongod, try next");
					if (!sendTermToProcess()) {
						LOGGER.warn("could not stop mongod, try next");
						if (!tryKillToProcess()) {
							LOGGER.warn("could not stop mongod the second time, try one last thing");
						}
					}
				}
			}
	}

	private long getProcessId() {
		return mongodProcessId;
	}

	protected boolean sendKillToProcess() {
		return getProcessId() > 0 && Processes.killProcess(supportConfig, platform,
			StreamToLineProcessor.wrap(commandOutput), getProcessId());
	}

	protected boolean sendTermToProcess() {
		return getProcessId() > 0 && Processes.termProcess(supportConfig, platform,
			StreamToLineProcessor.wrap(commandOutput), getProcessId());
	}

	protected boolean tryKillToProcess() {
		return getProcessId() > 0 && Processes.tryKillProcess(supportConfig, platform,
			StreamToLineProcessor.wrap(commandOutput), getProcessId());
	}

	protected final boolean sendStopToMongoInstance() {
		try {
			return Mongod.sendShutdownLegacy(net.getServerAddress(), net.getPort())
				|| Mongod.sendShutdown(net.getServerAddress(), net.getPort());
		} catch (UnknownHostException e) {
			LOGGER.error("sendStop", e);
		}
		return false;
	}

	public static RunningProcessFactory<RunningMongodProcess> factory(long startupTimeout, SupportConfig supportConfig, Platform platform, Net net) {
		return (process, processOutput, pidFile, timeout) -> {

//			LogWatchStreamProcessor logWatch = new LogWatchStreamProcessor(successMessage(), knownFailureMessages(),
//				StreamToLineProcessor.wrap(processOutput.output()));
			SuccessMessageLineListener logWatch = SuccessMessageLineListener.of(successMessage(), knownFailureMessages(), "error");

			ReaderProcessor output = Processors.connect(process.getReader(), new ListeningStreamProcessor(StreamToLineProcessor.wrap(processOutput.output()), logWatch::inspect));
			ReaderProcessor error = Processors.connect(process.getError(), StreamToLineProcessor.wrap(processOutput.error()));
			Runnable closeAllOutputs = () -> ReaderProcessor.abortAll(output, error);

			logWatch.waitForResult(startupTimeout);
			if (logWatch.successMessageFound()) {
				int pid = Mongod.getMongodProcessId(logWatch.allLines(), -1);
				return new RunningMongodProcess(process, pidFile, timeout, closeAllOutputs, supportConfig, platform, net, processOutput.commands(), pid);

			} else {
				String failureFound = logWatch.errorMessage().isPresent()
					? logWatch.errorMessage().get()
					: "\n" +
						"----------------------\n" +
						"Hmm.. no failure message.. \n" +
						"...the cause must be somewhere in the process output\n" +
						"----------------------\n" +
						""+logWatch.allLines();

				return Try.<RunningMongodProcess, RuntimeException>supplier(() -> {
					throw new RuntimeException("Could not start process: "+failureFound);
				})
					.andFinally(() -> process.stop(timeout))
					.andFinally(closeAllOutputs)
					.get();
			}
		};
	}

	private static List<String> successMessage() {
		// old: waiting for connections on port
		// since 4.4.5: Waiting for connections
		return Arrays.asList("aiting for connections");
	}

	private static List<String> knownFailureMessages() {
		return Arrays.asList(
			"(?<error>failed errno)",
			"ERROR:(?<error>.*)",
			"(?<error>error command line)",
			"(?<error>Address already in use)"
		);
	}

}
