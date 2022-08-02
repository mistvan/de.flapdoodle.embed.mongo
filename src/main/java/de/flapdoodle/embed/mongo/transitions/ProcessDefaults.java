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
