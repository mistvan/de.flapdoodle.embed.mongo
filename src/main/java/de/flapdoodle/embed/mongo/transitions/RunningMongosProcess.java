/*
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

import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.process.config.SupportConfig;
import de.flapdoodle.embed.process.io.StreamProcessor;
import de.flapdoodle.embed.process.runtime.ProcessControl;
import de.flapdoodle.embed.process.types.RunningProcessFactory;
import de.flapdoodle.os.Platform;

import java.nio.file.Path;

public class RunningMongosProcess extends RunningMongoProcess {

	public RunningMongosProcess(
		ProcessControl process,
		Path pidFile,
		long timeout,
		Runnable onStop,
		SupportConfig supportConfig,
		Platform platform,
		Net net,
		StreamProcessor commandOutput,
		int mongodProcessId
//		boolean withAuthEnabled
	) {
		super("mongos", process, pidFile, timeout, onStop, supportConfig, platform, net, commandOutput, mongodProcessId);
	}

	public static RunningProcessFactory<RunningMongosProcess> factory(long startupTimeout, SupportConfig supportConfig, Platform platform, Net net) {
		return RunningMongoProcess.factory(RunningMongosProcess::new, startupTimeout, supportConfig, platform, net);
	}
}
