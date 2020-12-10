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
import java.util.OptionalInt;

import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.process.config.SupportConfig;

@Immutable
public interface MongoDumpConfig extends MongoCommonConfig {

	@Default
	default boolean isVerbose() {
		return false;
	}

	Optional<String> getDatabaseName();

	Optional<String> getCollectionName();

	Optional<String> getQuery();

	Optional<String> getQueryFile();

	Optional<String> getReadPreference();

	@Default
	default boolean isForceTableScan() {
		return false;
	}

	Optional<String> getArchive();

	@Default
	default boolean isDumpDbUsersAndRoles() {
		return false;
	}

	@Default
	default boolean isGzip() {
		return false;
	}

	@Default
	default boolean isRepair() {
		return false;
	}

	Optional<String> getOut();

	@Default
	default boolean isOplog() {
		return false;
	}

	Optional<String> getExcludeCollection();

	Optional<String> getExcludeCollectionWithPrefix();

	OptionalInt getNumberOfParallelCollections();
	
	@Default
	@Override
	default String pidFile() {
		return "mongodump.pid";
	}

	@Override
	@Default
	default SupportConfig supportConfig() {
		return new de.flapdoodle.embed.mongo.config.SupportConfig(Command.MongoDump);
	}

	public static ImmutableMongoDumpConfig.Builder builder() {
		return ImmutableMongoDumpConfig.builder();
	}
}
