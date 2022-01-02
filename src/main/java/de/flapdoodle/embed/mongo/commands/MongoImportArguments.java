package de.flapdoodle.embed.mongo.commands;

import com.mongodb.ServerAddress;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
public abstract class MongoImportArguments {
	@Value.Default
	public boolean isVerbose() { return false; }

	public abstract Optional<String> databaseName();

	public abstract Optional<String> collectionName();

	public abstract Optional<String> importFile();

	@Value.Default
	public Optional<String> type() {
		return Optional.of("json");
	}

	@Value.Default
	public boolean isHeaderline() {
		return false;
	}

	@Value.Default
	public boolean isJsonArray() {
		return false;
	}

	@Value.Default
	public boolean dropCollection() {
		return false;
	}

	@Value.Default
	public boolean upsertDocuments() {
		return false;
	}

	@Value.Auxiliary
	public List<String> asArguments(ServerAddress serverAddress) {
		return getCommandLine(this,serverAddress);
	}

	public static ImmutableMongoImportArguments.Builder builder() {
		return ImmutableMongoImportArguments.builder();
	}

	public static ImmutableMongoImportArguments defaults() {
		return builder().build();
	}

	private static List<String> getCommandLine(MongoImportArguments config, ServerAddress serverAddress) {
		Arguments.Builder builder = Arguments.builder();

		builder.addIf(config.isVerbose(),"-v");
		builder.add("--port",""+serverAddress.getPort());
		builder.add("--host", serverAddress.getHost());
//		if (net.isIpv6()) {
//			builder.add("--ipv6");
//		}

		config.databaseName().ifPresent(it -> builder.add("--db", it));
		config.collectionName().ifPresent(it -> builder.add("--collection", it));

		builder.addIf(config.isJsonArray(), "--jsonArray");
		builder.addIf(config.dropCollection(), "--drop");
		builder.addIf(config.upsertDocuments(), "--upsert");

		config.importFile().ifPresent(it -> builder.add("--file", it));
		builder.addIf(config.isHeaderline(), "--headerline");
		config.type().ifPresent(it -> builder.add("--type",it));

		return builder.build();
	}

}
