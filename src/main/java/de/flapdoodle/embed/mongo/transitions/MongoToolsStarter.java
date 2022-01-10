package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.process.archives.ExtractedFileSet;
import de.flapdoodle.embed.process.config.SupportConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.types.ProcessConfig;
import de.flapdoodle.embed.process.types.RunningProcess;
import de.flapdoodle.embed.process.types.RunningProcessFactory;
import de.flapdoodle.reverse.State;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.StateLookup;
import org.immutables.value.Value;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class MongoToolsStarter<T extends RunningProcess> implements CommonStarter<T> {

	@Override
	public abstract StateID<T> destination();

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

	@Value.Auxiliary
	protected abstract RunningProcessFactory<T> processfactory();

	@Override
	public State<T> result(StateLookup lookup) {
		ExtractedFileSet fileSet = lookup.of(processExecutable());
		List<String> arguments = lookup.of(arguments()).value();
		Map<String, String> environment = lookup.of(processEnv()).value();
		ProcessConfig processConfig = lookup.of(processConfig());
		ProcessOutput processOutput = lookup.of(processOutput());
		SupportConfig supportConfig = lookup.of(supportConfig());

		try {

			T running = RunningProcess.start(processfactory(), fileSet.executable(), arguments, environment, processConfig,
				processOutput, supportConfig);

			return State.of(running, RunningProcess::stop);
		}
		catch (IOException ix) {
			throw new RuntimeException("could not start process", ix);
		}
	}
}
