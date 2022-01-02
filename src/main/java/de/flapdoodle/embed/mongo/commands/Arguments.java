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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class Arguments {

	public static class Builder {
		private final List<String> arguments = new ArrayList<>();

		public List<String> build() {
			return Collections.unmodifiableList(new ArrayList<>(arguments));
		}
		public Builder add(String... parts) {
			for (String part : parts) arguments.add(part);
			return this;
		}

		public Builder addIf(boolean condition, String ... parts) {
			if (condition) add(parts);
			return this;
		}
	}

	public static Builder builder() {
		return new Builder();
	}
}
