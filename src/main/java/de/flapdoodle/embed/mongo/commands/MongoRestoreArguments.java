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
package de.flapdoodle.embed.mongo.commands;

import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

@Value.Immutable
public abstract class MongoRestoreArguments implements MongoToolsArguments {

	@Value.Default
	public boolean verbose() {
		return false;
	}

	public abstract Optional<String> databaseName();

	public abstract Optional<String> collectionName();

	public abstract OptionalLong oplogLimit();
	public abstract Optional<String> archive();
	public abstract Optional<String> dir();

	public abstract OptionalInt numberOfParallelCollections();
	public abstract OptionalInt numberOfInsertionWorkersPerCollection();

	public abstract Optional<String> writeConcern();

	@Value.Default
	public boolean objectCheck() {
		return false;
	}
	@Value.Default
	public boolean oplogReplay() {
		return false;
	}
	@Value.Default
	public boolean restoreDbUsersAndRoles() {
		return false;
	}
	@Value.Default
	public boolean gzip() {
		return false;
	}

	@Value.Default
	public boolean dropCollection() {
		return false;
	};

	@Value.Default
	public boolean noIndexRestore() {
		return false;
	}
	@Value.Default
	public boolean noOptionsRestore() {
		return false;
	}
	@Value.Default
	public boolean keepIndexVersion() {
		return false;
	}
	@Value.Default
	public boolean maintainInsertionOrder() {
		return false;
	}
	@Value.Default
	public boolean stopOnError() {
		return false;
	}
	@Value.Default
	public boolean bypassDocumentValidation() {
		return false;
	}

	@Override
	@Value.Auxiliary
	public List<String> asArguments(ServerAddress serverAddress) {
		return getCommandLine(this,serverAddress);
	}

	public static ImmutableMongoRestoreArguments.Builder builder() {
		return ImmutableMongoRestoreArguments.builder();
	}

	public static ImmutableMongoRestoreArguments defaults() {
		return builder().build();
	}

	private static List<String> getCommandLine(MongoRestoreArguments config, ServerAddress serverAddress) {
		Arguments.Builder ret = Arguments.builder();

		ret.addIf(config.verbose(),"-v");
		ret.add("--port",""+serverAddress.getPort());
		ret.add("--host", serverAddress.getHost());

		ret.addIf("--db", config.databaseName());
		ret.addIf("--collection", config.collectionName());

		ret.addIf(config.objectCheck(), "--objCheck");
		ret.addIf(config.oplogReplay(), "--oplogReplay");

		config.oplogLimit().ifPresent(it -> ret.add("--oplogLimit", ""+it));
		config.archive().ifPresent(it -> ret.add("--archive="+it));

		ret.addIf(config.restoreDbUsersAndRoles(), "--restoreDbUsersAndRoles");
		ret.addIf("--dir", config.dir());
		ret.addIf(config.gzip(), "--gzip");
		ret.addIf(config.dropCollection(), "--drop");
		ret.addIf("--writeConcern", config.writeConcern());
		ret.addIf(config.noIndexRestore(), "--noIndexRestore");
		ret.addIf(config.noOptionsRestore(), "--noOptionsRestore");
		ret.addIf(config.keepIndexVersion(), "--keepIndexVersion");
		ret.addIf(config.maintainInsertionOrder(), "--maintainInsertionOrder");

		config.numberOfParallelCollections().ifPresent(it -> ret.add("--numParallelCollections",""+it));
		config.numberOfInsertionWorkersPerCollection().ifPresent(it -> ret.add("--numInsertionWorkersPerCollection",""+it));

		ret.addIf(config.stopOnError(), "--stopOnError");
		ret.addIf(config.bypassDocumentValidation(), "--bypassDocumentValidation");

		return ret.build();
	}
}
