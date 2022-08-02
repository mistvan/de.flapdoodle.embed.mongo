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

import de.flapdoodle.embed.mongo.packageresolver.Command;
import de.flapdoodle.embed.process.config.SupportConfig;
import de.flapdoodle.embed.process.config.process.ProcessOutput;
import de.flapdoodle.embed.process.types.Name;
import de.flapdoodle.embed.process.types.ProcessConfig;
import de.flapdoodle.embed.process.types.ProcessEnv;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.reverse.Transitions;
import de.flapdoodle.reverse.transitions.Derive;
import de.flapdoodle.reverse.transitions.ImmutableStart;
import de.flapdoodle.reverse.transitions.Start;

import java.util.Collections;

public interface ProcessDefaults {
	default ImmutableStart<ProcessConfig> processConfig() {
		return Start.to(ProcessConfig.class).initializedWith(ProcessConfig.defaults()).withTransitionLabel("create default");
	}

	default Transition<ProcessEnv> processEnv() {
		return Start.to(ProcessEnv.class).initializedWith(ProcessEnv.of(Collections.emptyMap())).withTransitionLabel("create empty env");
	}

	default Transition<ProcessOutput> processOutput() {
		return Derive.given(Name.class).state(ProcessOutput.class)
			.deriveBy(name -> ProcessOutput.namedConsole(name.value()))
			.withTransitionLabel("create named console");
	}

	default Transition<SupportConfig> supportConfig() {
		return Derive.given(Command.class).state(SupportConfig.class)
			.deriveBy(c -> SupportConfig.builder()
				.name(c.commandName())
				.messageOnException((clazz, ex) -> null)
				.supportUrl("https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo/issues")
				.build()).withTransitionLabel("create default");
	}

	default Transitions processDefaults() {
		return Transitions.from(
			processConfig(),
			processEnv(),
			processOutput(),
			supportConfig()
		);
	}
}
