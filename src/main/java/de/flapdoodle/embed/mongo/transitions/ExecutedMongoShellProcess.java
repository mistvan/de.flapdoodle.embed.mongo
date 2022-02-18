package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.process.transitions.Executer;
import de.flapdoodle.embed.process.types.ExecutedProcess;
import de.flapdoodle.embed.process.types.RunningProcess;
import de.flapdoodle.embed.process.types.RunningProcessImpl;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.Transition;
import org.immutables.value.Value;

@Value.Immutable
public abstract class ExecutedMongoShellProcess implements ExecutedProcess {

	public static ExecutedMongoShellProcess stop(RunningProcess process) {
		return ImmutableExecutedMongoShellProcess.builder()
			.returnCode(process.stop())
			.build();
	}

	public static Transition<ExecutedMongoShellProcess> withDefaults() {
		return Executer.with(RunningProcessImpl::new, ExecutedMongoShellProcess::stop)
			.destination(StateID.of(ExecutedMongoShellProcess.class))
			.build();
	}
}
