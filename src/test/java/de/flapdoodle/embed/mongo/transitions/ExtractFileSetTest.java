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

import com.google.common.collect.ImmutableMap;
import de.flapdoodle.embed.process.io.directories.PersistentDir;
import de.flapdoodle.reverse.State;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.StateLookup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ExtractFileSetTest {

	@Test
	public void overridePersistentPathWithSystemEnv(@TempDir Path tempDir) {
		ExtractFileSet noEnvTestee = new ExtractFileSet() {
			@Override public Map<String, String> systemEnv() {
				return ImmutableMap.of();
			}
		};

		ExtractFileSet withEnvTestee = new ExtractFileSet() {
			@Override public Map<String, String> systemEnv() {
				return ImmutableMap.of("EMBEDDED_MONGO_ARTIFACTS", tempDir.toString());
			}
		};

		StateLookup lookupMock = new StateLookup() {
			@Override public <D> D of(StateID<D> type) {
				throw new IllegalArgumentException("should not be called");
			}
		};

		State<PersistentDir> result = noEnvTestee.persistentBaseDir().result(lookupMock);
		assertThat(result.value().value()).isNotEqualTo(tempDir);

		result = withEnvTestee.persistentBaseDir().result(lookupMock);
		assertThat(result.value().value()).isEqualTo(tempDir);
	}
}