package de.flapdoodle.embed.mongo.transitions;

import com.mongodb.ServerAddress;
import de.flapdoodle.checks.Preconditions;
import de.flapdoodle.embed.mongo.commands.ImmutableMongoImportArguments;
import de.flapdoodle.embed.mongo.commands.MongoImportArguments;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
import de.flapdoodle.embed.mongo.types.DatabaseDir;
import de.flapdoodle.embed.process.archives.ExtractedFileSet;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.embed.process.types.ProcessArguments;
import de.flapdoodle.os.Platform;
import de.flapdoodle.reverse.State;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.StateLookup;
import de.flapdoodle.reverse.naming.HasLabel;
import org.immutables.value.Value;

import java.util.List;
import java.util.Set;

@Value.Immutable
public abstract class MongoImportProcessArguments implements CommandProcessArguments<MongoImportArguments>, HasLabel {

	@Override
	@Value.Auxiliary
	public String transitionLabel() {
		return "Create mongoImport arguments";
	}

	@Override
	@Value.Default
	public StateID<ProcessArguments> destination() {
		return StateID.of(ProcessArguments.class);
	}

	@Override
	@Value.Default
	public StateID<MongoImportArguments> arguments() {
		return StateID.of(MongoImportArguments.class);
	}

	@Value.Default
	public StateID<ServerAddress> serverAddress() {
		return StateID.of(ServerAddress.class);
	}

	@Override
	@Value.Auxiliary
	public Set<StateID<?>> sources() {
		return StateID.setOf(arguments(), serverAddress());
	}

	@Override
	public State<ProcessArguments> result(StateLookup lookup) {
		MongoImportArguments arguments = lookup.of(arguments());
		ServerAddress serverAddress = lookup.of(serverAddress());

		List<String> commandLine = arguments.asArguments(serverAddress);
		return State.of(ProcessArguments.of(commandLine));
	}

	public static ImmutableMongoImportProcessArguments withDefaults() {
		return builder().build();
	}

	public static ImmutableMongoImportProcessArguments.Builder builder() {
		return ImmutableMongoImportProcessArguments.builder();
	}
}
