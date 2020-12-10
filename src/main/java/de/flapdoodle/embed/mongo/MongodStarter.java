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
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.ExtractedFileSet;
import de.flapdoodle.embed.process.runtime.Starter;

/**
 *
 */
public class MongodStarter extends Starter<MongodConfig,MongodExecutable,MongodProcess> {

	private MongodStarter(RuntimeConfig config) {
		super(config);
	}

	public static MongodStarter getInstance(RuntimeConfig config) {
		return new MongodStarter(config);
	}

	public static MongodStarter getDefaultInstance() {
		return getInstance(Defaults.runtimeConfigFor(Command.MongoD).build());
	}
	
	@Override
	protected MongodExecutable newExecutable(MongodConfig mongodConfig, Distribution distribution, RuntimeConfig runtime, ExtractedFileSet files) {
		return new MongodExecutable(distribution, mongodConfig, runtime, files);
	}
}
