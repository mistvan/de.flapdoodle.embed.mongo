package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.mongo.commands.MongoDumpArguments;
import de.flapdoodle.embed.mongo.commands.MongoRestoreArguments;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.naming.HasLabel;
import org.immutables.value.Value;

@Value.Immutable
public abstract class MongoRestoreProcessArguments extends MongoToolsProcessArguments<MongoRestoreArguments> implements HasLabel {

	@Override
	@Value.Auxiliary
	public String transitionLabel() {
		return "Create mongoRestore arguments";
	}

	@Override
	@Value.Default
	public StateID<MongoRestoreArguments> arguments() {
		return StateID.of(MongoRestoreArguments.class);
	}

	public static ImmutableMongoRestoreProcessArguments withDefaults() {
		return builder().build();
	}

	public static ImmutableMongoRestoreProcessArguments.Builder builder() {
		return ImmutableMongoRestoreProcessArguments.builder();
	}
}
