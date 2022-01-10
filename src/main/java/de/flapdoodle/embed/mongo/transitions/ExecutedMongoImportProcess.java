package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.process.transitions.Executer;
import de.flapdoodle.embed.process.types.ExecutedProcess;
import de.flapdoodle.embed.process.types.RunningProcess;
import de.flapdoodle.embed.process.types.RunningProcessImpl;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.Transition;
import org.immutables.value.Value;

@Value.Immutable
public abstract class ExecutedMongoImportProcess implements ExecutedProcess {

	public static ExecutedMongoImportProcess stop(RunningProcess process) {
		return ImmutableExecutedMongoImportProcess.builder()
			.returnCode(process.stop())
			.build();
	}

	public static Transition<ExecutedMongoImportProcess> withDefaults() {
		return Executer.with(RunningProcessImpl::new, ExecutedMongoImportProcess::stop)
			.destination(StateID.of(ExecutedMongoImportProcess.class))
			.build();
	}
}
