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

import de.flapdoodle.embed.mongo.config.Defaults;
import de.flapdoodle.embed.mongo.config.MongoShellConfig;
import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.ExtractedFileSet;
import de.flapdoodle.embed.process.runtime.Starter;

public class MongoShellStarter extends Starter<MongoShellConfig, MongoShellExecutable ,MongoShellProcess> {

	private MongoShellStarter(RuntimeConfig config) {
		super(config);
	}

	public static MongoShellStarter getInstance(RuntimeConfig config) {
		return new MongoShellStarter(config);
	}

	public static MongoShellStarter getDefaultInstance() {
		return getInstance(Defaults.runtimeConfigFor(Command.Mongo)
				.isDaemonProcess(false)
				.build());
	}

	@Override
	protected MongoShellExecutable newExecutable(MongoShellConfig mongoShellConfig, Distribution distribution, RuntimeConfig runtime, ExtractedFileSet files) {
		return new MongoShellExecutable(distribution, mongoShellConfig, runtime, files);
	}
}
