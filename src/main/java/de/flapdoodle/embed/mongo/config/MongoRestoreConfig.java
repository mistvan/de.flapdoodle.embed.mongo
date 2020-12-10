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
import java.util.OptionalLong;

import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.process.config.SupportConfig;

@Immutable
public interface MongoRestoreConfig extends MongoCommonConfig {
	@Default
   default boolean isVerbose() {
	   return false;
   }
	
   Optional<String> getDatabaseName();
   Optional<String> getCollectionName();
   OptionalLong getOplogLimit();
   Optional<String> getArchive();
   Optional<String> getDir();
   
   OptionalInt getNumberOfParallelCollections();
   OptionalInt getNumberOfInsertionWorkersPerCollection();
   
   Optional<String> getWriteConcern();
   
   @Default
   default boolean isObjectCheck() {
	   return false;
   }
   @Default
   default boolean isOplogReplay() {
	   return false;
   }
   @Default
   default boolean isRestoreDbUsersAndRoles() {
	   return false;
   }
   @Default
   default boolean isGzip() {
	   return false;
   }
   boolean isDropCollection();
   
   @Default
   default boolean isNoIndexRestore() {
	   return false;
   }
   @Default
   default boolean isNoOptionsRestore() {
	   return false;
   }
   @Default
   default boolean isKeepIndexVersion() {
	   return false;
   }
   @Default
   default boolean isMaintainInsertionOrder() {
	   return false;
   }
   @Default
   default boolean isStopOnError() {
	   return false;
   }
   @Default
   default boolean isBypassDocumentValidation() {
	   return false;
   }
   
	@Default
	@Override
	default String pidFile() {
		return "mongorestore.pid";
	}

	@Override
	@Default
	default SupportConfig supportConfig() {
		return new de.flapdoodle.embed.mongo.config.SupportConfig(Command.MongoRestore);
	}
	
	public static ImmutableMongoRestoreConfig.Builder builder() {
		return ImmutableMongoRestoreConfig.builder();
	}
}
