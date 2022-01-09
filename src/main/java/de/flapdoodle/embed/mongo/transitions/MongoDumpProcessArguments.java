package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.mongo.commands.MongoDumpArguments;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.naming.HasLabel;
import org.immutables.value.Value;

@Value.Immutable
public abstract class MongoDumpProcessArguments extends MongoToolsProcessArguments<MongoDumpArguments> implements HasLabel {

	@Override
	@Value.Auxiliary
	public String transitionLabel() {
		return "Create mongoDump arguments";
	}

	@Override
	@Value.Default
	public StateID<MongoDumpArguments> arguments() {
		return StateID.of(MongoDumpArguments.class);
	}

	public static ImmutableMongoDumpProcessArguments withDefaults() {
		return builder().build();
	}

	public static ImmutableMongoDumpProcessArguments.Builder builder() {
		return ImmutableMongoDumpProcessArguments.builder();
	}
}
