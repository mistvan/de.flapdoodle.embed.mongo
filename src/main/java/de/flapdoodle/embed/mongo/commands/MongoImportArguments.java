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
package de.flapdoodle.embed.mongo.commands;

import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
public abstract class MongoImportArguments implements MongoToolsArguments {
	@Value.Default
	public boolean verbose() { return false; }

	public abstract Optional<String> databaseName();

	public abstract Optional<String> collectionName();

	public abstract Optional<String> importFile();

	@Value.Default
	public Optional<String> type() {
		return Optional.of("json");
	}

	@Value.Default
	public boolean isHeaderline() {
		return false;
	}

	@Value.Default
	public boolean isJsonArray() {
		return false;
	}

	@Value.Default
	public boolean dropCollection() {
		return false;
	}

	@Value.Default
	public boolean upsertDocuments() {
		return false;
	}

	@Override
	@Value.Auxiliary
	public List<String> asArguments(ServerAddress serverAddress) {
		return getCommandLine(this,serverAddress);
	}

	public static ImmutableMongoImportArguments.Builder builder() {
		return ImmutableMongoImportArguments.builder();
	}

	public static ImmutableMongoImportArguments defaults() {
		return builder().build();
	}

	private static List<String> getCommandLine(MongoImportArguments config, ServerAddress serverAddress) {
		Arguments.Builder builder = Arguments.builder();

		builder.addIf(config.verbose(),"-v");
		builder.add("--port",""+serverAddress.getPort());
		builder.add("--host", serverAddress.getHost());
//		if (net.isIpv6()) {
//			builder.add("--ipv6");
//		}

		builder.addIf("--db", config.databaseName());
		builder.addIf("--collection", config.collectionName());


		builder.addIf(config.isJsonArray(), "--jsonArray");
		builder.addIf(config.dropCollection(), "--drop");
		builder.addIf(config.upsertDocuments(), "--upsert");

		builder.addIf("--file", config.importFile());
		builder.addIf(config.isHeaderline(), "--headerline");
		builder.addIf("--type", config.type());

		return builder.build();
	}

}
