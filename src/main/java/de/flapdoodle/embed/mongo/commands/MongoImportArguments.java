package de.flapdoodle.embed.mongo.commands;

import com.mongodb.ServerAddress;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
public abstract class MongoImportArguments implements MongoToolsArguments {
	@Value.Default
	public boolean verbose() { return false; }

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

	@Override
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

		builder.addIf(config.verbose(),"-v");
		builder.add("--port",""+serverAddress.getPort());
		builder.add("--host", serverAddress.getHost());
//		if (net.isIpv6()) {
//			builder.add("--ipv6");
//		}

		builder.addIf("--db", config.databaseName());
		builder.addIf("--collection", config.collectionName());


		builder.addIf(config.isJsonArray(), "--jsonArray");
		builder.addIf(config.dropCollection(), "--drop");
		builder.addIf(config.upsertDocuments(), "--upsert");

		builder.addIf("--file", config.importFile());
		builder.addIf(config.isHeaderline(), "--headerline");
		builder.addIf("--type", config.type());

		return builder.build();
	}

}
