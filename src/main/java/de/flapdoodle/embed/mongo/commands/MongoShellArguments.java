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

import com.mongodb.ServerAddress;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
public abstract class MongoShellArguments implements MongoToolsArguments {
	public abstract List<String> scriptParameters();

	public abstract Optional<String> scriptName();

	public abstract Optional<String> dbName();

	public abstract Optional<String> password();

	public abstract Optional<String> userName();

	@Override
	public List<String> asArguments(ServerAddress serverAddress) {
		return getCommandLine(this, serverAddress);
	}

	public static ImmutableMongoShellArguments.Builder builder() {
		return ImmutableMongoShellArguments.builder();
	}

	public static MongoShellArguments defaults() {
		return builder().build();
	}
	
	private static List<String> getCommandLine(
		MongoShellArguments config,
		ServerAddress serverAddress
	)
		 {
		Arguments.Builder ret = Arguments.builder();

		String hostname=serverAddress.getHost();
		int port = serverAddress.getPort();

		ret.addIf("--username", config.userName());
		ret.addIf("--password", config.password());

		if (config.dbName().isPresent()) {
			 ret.add(hostname+":"+port+"/"+config.dbName().get());
		} else {
			ret.add(hostname+":"+port);
		}

		if (!config.scriptParameters().isEmpty()) {
			ret.add("--eval");
			StringBuilder eval = new StringBuilder();
			for (String parameter : config.scriptParameters()) {
				eval.append(parameter).append("; ");
			}
			ret.add(eval.toString());
		}
		if (config.scriptName().isPresent()) {
			ret.add(config.scriptName().get());
		}

		return ret.build();
	}

}
