package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.mongo.commands.MongoShellArguments;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.naming.HasLabel;
import org.immutables.value.Value;

@Value.Immutable
public abstract class MongoShellProcessArguments extends MongoToolsProcessArguments<MongoShellArguments> implements HasLabel {

	@Override
	@Value.Auxiliary
	public String transitionLabel() {
		return "Create mongoDump arguments";
	}

	@Override
	@Value.Default
	public StateID<MongoShellArguments> arguments() {
		return StateID.of(MongoShellArguments.class);
	}

	public static ImmutableMongoShellProcessArguments withDefaults() {
		return builder().build();
	}

	public static ImmutableMongoShellProcessArguments.Builder builder() {
		return ImmutableMongoShellProcessArguments.builder();
	}
}
