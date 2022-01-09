package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.process.archives.ExtractedFileSet;
import de.flapdoodle.embed.process.config.SupportConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.types.ProcessArguments;
import de.flapdoodle.embed.process.types.ProcessConfig;
import de.flapdoodle.embed.process.types.ProcessEnv;
import de.flapdoodle.embed.process.types.RunningProcess;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.Transition;
import org.immutables.value.Value;

public interface CommonStarter<T extends RunningProcess> extends Transition<T> {
	@Value.Default
	default StateID<ExtractedFileSet> processExecutable() {
		return StateID.of(ExtractedFileSet.class);
	}

	@Value.Default
	default StateID<ProcessConfig> processConfig() {
		return StateID.of(ProcessConfig.class);
	}

	@Value.Default
	default StateID<ProcessEnv> processEnv() {
		return StateID.of(ProcessEnv.class);
	}

	@Value.Default
	default StateID<ProcessArguments> arguments() {
		return StateID.of(ProcessArguments.class);
	}

	@Value.Default
	default StateID<ProcessOutput> processOutput() {
		return StateID.of(ProcessOutput.class);
	}

	@Value.Default
	default StateID<SupportConfig> supportConfig() {
		return StateID.of(SupportConfig.class);
	}
}
