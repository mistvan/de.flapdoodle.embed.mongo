package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.process.archives.ExtractedFileSet;
import de.flapdoodle.embed.process.config.SupportConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.types.*;
import de.flapdoodle.reverse.State;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.StateLookup;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.reverse.naming.HasLabel;
import org.immutables.value.Value;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Value.Immutable
public abstract class MongoImportStarter implements Transition<RunningMongoImportProcess>, HasLabel {

	@Override
	@Value.Default
	public String transitionLabel() {
		return "Start MongoImport";
	}

	@Value.Default
	public StateID<ExtractedFileSet> processExecutable() {
		return StateID.of(ExtractedFileSet.class);
	}

	@Value.Default
	public StateID<ProcessConfig> processConfig() {
		return StateID.of(ProcessConfig.class);
	}

	@Value.Default
	public StateID<ProcessEnv> processEnv() {
		return StateID.of(ProcessEnv.class);
	}

	@Value.Default
	public StateID<ProcessArguments> arguments() {
		return StateID.of(ProcessArguments.class);
	}

	@Value.Default
	public StateID<ProcessOutput> processOutput() {
		return StateID.of(ProcessOutput.class);
	}

	@Value.Default
	public StateID<SupportConfig> supportConfig() {
		return StateID.of(SupportConfig.class);
	}

	@Override
	@Value.Default
	public StateID<RunningMongoImportProcess> destination() {
		return StateID.of(RunningMongoImportProcess.class);
	}

	@Override
	public Set<StateID<?>> sources() {
		return StateID.setOf(
			processExecutable(),
			processConfig(),
			processEnv(),
			arguments(),
			processOutput(),
			supportConfig()
		);
	}

	@Override
	public State<RunningMongoImportProcess> result(StateLookup lookup) {
		ExtractedFileSet fileSet = lookup.of(processExecutable());
		List<String> arguments = lookup.of(arguments()).value();
		Map<String, String> environment = lookup.of(processEnv()).value();
		ProcessConfig processConfig = lookup.of(processConfig());
		ProcessOutput processOutput = lookup.of(processOutput());
		SupportConfig supportConfig = lookup.of(supportConfig());

		try {
			RunningProcessFactory<RunningMongoImportProcess> factory = RunningMongoImportProcess.factory();

			RunningMongoImportProcess running = RunningProcess.start(factory, fileSet.executable(), arguments, environment, processConfig,
				processOutput, supportConfig);

			return State.of(running, RunningProcess::stop);
		}
		catch (IOException ix) {
			throw new RuntimeException("could not start process", ix);
		}
	}

	public static ImmutableMongoImportStarter.Builder builder() {
		return ImmutableMongoImportStarter.builder();
	}

	public static ImmutableMongoImportStarter withDefaults() {
		return builder().build();
	}
}
