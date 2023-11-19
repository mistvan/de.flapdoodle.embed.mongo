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
package de.flapdoodle.embed.mongo.client;

import org.immutables.value.Value;

import java.util.Collections;
import java.util.List;

@Value.Immutable
public abstract class AuthenticationSetup {
	@Value.Parameter
	protected abstract UsernamePassword admin();

	@Value.Default
	protected List<Entry> entries() {
		return Collections.emptyList();
	}

	public interface Entry {

	}

	@Value.Immutable
	public interface Role extends Entry {
		@Value.Parameter
		String database();
		@Value.Parameter
		String collection();
		@Value.Parameter
		String name();
		List<String> actions();
	}

	@Value.Immutable
	public interface User extends Entry {
		@Value.Parameter
		String database();
		@Value.Parameter
		UsernamePassword user();
		List<String> roles();
	}

	public static ImmutableRole role(String database, String collection, String name) {
		return ImmutableRole.of(database, collection, name);
	}

	public static ImmutableUser user(String database, UsernamePassword usernamePassword) {
		return ImmutableUser.of(database, usernamePassword);
	}

	public static ImmutableAuthenticationSetup of(UsernamePassword adminUsernamePassword) {
		return ImmutableAuthenticationSetup.of(adminUsernamePassword);
	}

}
