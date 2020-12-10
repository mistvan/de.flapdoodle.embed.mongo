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

import java.util.Optional;

import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;

@Immutable
public interface MongoCmdOptions {
	
	@Default 
	default int syncDelay() {
		return 0;
	}
	
	@Default
	default boolean useDefaultSyncDelay() {
		return false;
	}

	Optional<String> storageEngine();

	@Default
	default boolean isVerbose() {
		return false;
	}

	@Default
	default boolean useNoPrealloc() {
		return true;
	}

	@Default
	default boolean useSmallFiles() {
		return true;
	}

	@Default
	default boolean useNoJournal() {
		return true;
	}

	@Default
	default boolean enableTextSearch() {
		return false;
	}

	@Default
	default boolean auth() {
		return false;
	}

	@Default
	default boolean master() {
		return false;
	}

	public static ImmutableMongoCmdOptions.Builder builder() {
		return ImmutableMongoCmdOptions.builder();
	}

	public static MongoCmdOptions defaults() {
		return ImmutableMongoCmdOptions.builder().build();
	}
}
