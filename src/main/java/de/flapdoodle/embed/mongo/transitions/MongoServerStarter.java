package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.process.archives.ExtractedFileSet;
import de.flapdoodle.embed.process.config.SupportConfig;
import de.flapdoodle.embed.process.config.process.ProcessOutput;
import de.flapdoodle.embed.process.types.*;
import de.flapdoodle.os.Platform;
import de.flapdoodle.reverse.State;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.StateLookup;
import de.flapdoodle.reverse.Transition;
import org.immutables.value.Value;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class MongoServerStarter<T extends RunningProcess> implements Transition<T> {
	@Value.Default
	public StateID<ExtractedFileSet> processExecutable() {
		return StateID.of(ExtractedFileSet.class);
	}

	public StateID<ProcessWorkingDir> processWorkingDir() {
		return StateID.of(ProcessWorkingDir.class);
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

	@Value.Default
	public StateID<Platform> platform() {
		return StateID.of(Platform.class);
	}

	@Value.Default
	public StateID<Net> net() {
		return StateID.of(Net.class);
	}

	@Override
	public Set<StateID<?>> sources() {
		return StateID.setOf(
			processWorkingDir(),
			processExecutable(),
			processConfig(),
			processEnv(),
			arguments(),
			processOutput(),
			supportConfig(),
			platform(),
			net()
		);
	}

	@Value.Auxiliary
	protected abstract RunningProcessFactory<T> factory(long startupTimeout, SupportConfig supportConfig, Platform platform, Net net);

	@Override
	public State<T> result(StateLookup lookup) {
		Path processWorkingDir = lookup.of(processWorkingDir()).value();
		ExtractedFileSet fileSet = lookup.of(processExecutable());
		List<String> arguments = lookup.of(arguments()).value();
		Map<String, String> environment = lookup.of(processEnv()).value();
		ProcessConfig processConfig = lookup.of(processConfig());
		ProcessOutput processOutput = lookup.of(processOutput());
		SupportConfig supportConfig = lookup.of(supportConfig());
		Platform platform = lookup.of(platform());
		Net net = lookup.of(net());

		try {
			RunningProcessFactory<T> factory = factory(20000, supportConfig, platform, net);

			T running = RunningProcess.start(factory, processWorkingDir,  fileSet.executable(), arguments, environment, processConfig,
				processOutput, supportConfig);

			return State.of(running, RunningProcess::stop);
		}
		catch (IOException ix) {
			throw new RuntimeException("could not start process", ix);
		}
	}

}
