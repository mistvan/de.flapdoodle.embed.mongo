package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.process.types.RunningProcessFactory;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.naming.HasLabel;
import org.immutables.value.Value;

@Value.Immutable
public abstract class MongoDumpStarter extends MongoToolsStarter<RunningMongoDumpProcess> implements HasLabel {
	
	@Override
	@Value.Default
	public String transitionLabel() {
		return "Start MongoDump";
	}

	@Override protected RunningProcessFactory<RunningMongoDumpProcess> processfactory() {
		return RunningMongoDumpProcess::new;
	}

	@Override
	@Value.Default
	public StateID<RunningMongoDumpProcess> destination() {
		return StateID.of(RunningMongoDumpProcess.class);
	}

	public static ImmutableMongoDumpStarter.Builder builder() {
		return ImmutableMongoDumpStarter.builder();
	}

	public static ImmutableMongoDumpStarter withDefaults() {
		return builder().build();
	}
}
