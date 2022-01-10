package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.process.transitions.Executer;
import de.flapdoodle.embed.process.types.ExecutedProcess;
import de.flapdoodle.embed.process.types.RunningProcess;
import de.flapdoodle.embed.process.types.RunningProcessImpl;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.Transition;
import org.immutables.value.Value;

@Value.Immutable
public abstract class ExecutedMongoDumpProcess implements ExecutedProcess {

	public static ExecutedMongoDumpProcess stop(RunningProcess process) {
		return ImmutableExecutedMongoDumpProcess.builder()
			.returnCode(process.stop())
			.build();
	}

	public static Transition<ExecutedMongoDumpProcess> withDefaults() {
		return Executer.with(RunningProcessImpl::new, ExecutedMongoDumpProcess::stop)
			.destination(StateID.of(ExecutedMongoDumpProcess.class))
			.build();
	}
}
