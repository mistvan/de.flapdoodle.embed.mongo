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
package de.flapdoodle.embed.mongo.distribution;

import java.util.Arrays;
import java.util.EnumSet;

public enum Feature {
	SYNC_DELAY, 
	TEXT_SEARCH /* enabled by default in mongodb >=2.6 */,
	STORAGE_ENGINE /* with >=3.0, default changed with 3.2 */,
	ONLY_64BIT /* mongodb 3.4 and beyond does not support 32 bit */, 
	NO_CHUNKSIZE_ARG /*mongos since 3.4? does not support --chunkSize argument */, 
	MONGOS_CONFIGDB_SET_STYLE /* mongos since 3.3.? */,
	NO_HTTP_INTERFACE_ARG /*not supported since 3.6 https://docs.mongodb.com/manual/release-notes/3.6-compatibility/*/,

	NO_SOLARIS_SUPPORT,
	NO_BIND_IP_TO_LOCALHOST,

	DISABLE_USE_PREALLOC, /* disable usePreAlloc >= 4.2.0 */
	DISABLE_USE_SMALL_FILES,  /* disable useSmallFiles >= 4.2.0 */
	;


	public static EnumSet<Feature> asSet(Feature... features) {
		if (features.length == 0) {
			return EnumSet.noneOf(Feature.class);
		}
		return EnumSet.copyOf(Arrays.asList(features));
	}
}
