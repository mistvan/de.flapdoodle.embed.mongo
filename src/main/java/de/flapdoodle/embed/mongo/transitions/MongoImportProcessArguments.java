package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.mongo.commands.MongoImportArguments;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.naming.HasLabel;
import org.immutables.value.Value;

@Value.Immutable
public abstract class MongoImportProcessArguments extends MongoToolsProcessArguments<MongoImportArguments> implements HasLabel {

	@Override
	@Value.Auxiliary
	public String transitionLabel() {
		return "Create mongoImport arguments";
	}

	@Override
	@Value.Default
	public StateID<MongoImportArguments> arguments() {
		return StateID.of(MongoImportArguments.class);
	}

	public static ImmutableMongoImportProcessArguments withDefaults() {
		return builder().build();
	}

	public static ImmutableMongoImportProcessArguments.Builder builder() {
		return ImmutableMongoImportProcessArguments.builder();
	}
}
