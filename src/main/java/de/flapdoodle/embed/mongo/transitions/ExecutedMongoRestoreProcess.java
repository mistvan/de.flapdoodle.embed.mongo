package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.process.transitions.Executer;
import de.flapdoodle.embed.process.types.ExecutedProcess;
import de.flapdoodle.embed.process.types.RunningProcess;
import de.flapdoodle.embed.process.types.RunningProcessImpl;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.Transition;
import org.immutables.value.Value;

@Value.Immutable
public abstract class ExecutedMongoRestoreProcess implements ExecutedProcess {

	public static ExecutedMongoRestoreProcess stop(RunningProcess process) {
		return ImmutableExecutedMongoRestoreProcess.builder()
			.returnCode(process.stop())
			.build();
	}

	public static Transition<ExecutedMongoRestoreProcess> withDefaults() {
		return Executer.with(RunningProcessImpl::new, ExecutedMongoRestoreProcess::stop)
			.destination(StateID.of(ExecutedMongoRestoreProcess.class))
			.build();
	}
}
