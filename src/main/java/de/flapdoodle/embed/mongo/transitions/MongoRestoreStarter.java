package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.process.types.RunningProcessFactory;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.naming.HasLabel;
import org.immutables.value.Value;

@Value.Immutable
public abstract class MongoRestoreStarter extends MongoToolsStarter<RunningMongoRestoreProcess> implements HasLabel {

	@Override
	@Value.Default
	public String transitionLabel() {
		return "Start MongoRestore";
	}

	@Override
	protected RunningProcessFactory<RunningMongoRestoreProcess> processfactory() {
		return RunningMongoRestoreProcess::new;
	}

	@Override
	@Value.Default
	public StateID<RunningMongoRestoreProcess> destination() {
		return StateID.of(RunningMongoRestoreProcess.class);
	}

	public static ImmutableMongoRestoreStarter.Builder builder() {
		return ImmutableMongoRestoreStarter.builder();
	}

	public static ImmutableMongoRestoreStarter withDefaults() {
		return builder().build();
	}
}
