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
package de.flapdoodle.embed.mongo.config;

import org.immutables.value.Value.Default;

import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
import de.flapdoodle.embed.process.config.ExecutableProcessConfig;

public interface MongoCommonConfig extends ExecutableProcessConfig {
	@Override
	public IFeatureAwareVersion version();

	@Default
	default Timeout timeout() {
		return new Timeout();
	}

	@Default
	default Net net() {
		return Net.defaults();
	}

	@Default
	default MongoCmdOptions cmdOptions() {
		return MongoCmdOptions.defaults();
	}

	@Default
	default String password() {
		return "";
	}

	@Default
	default String userName() {
		return "";
	}

    String pidFile();    
}
