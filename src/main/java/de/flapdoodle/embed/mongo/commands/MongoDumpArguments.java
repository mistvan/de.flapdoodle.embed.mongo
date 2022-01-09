package de.flapdoodle.embed.mongo.commands;

import com.mongodb.ServerAddress;
import org.immutables.value.Value;

import java.util.*;

@Value.Immutable
public abstract class MongoDumpArguments implements MongoToolsArguments  {

	@Value.Default
	public boolean verbose() {
		return false;
	}

	public abstract Optional<String> databaseName();

	public abstract Optional<String> collectionName();

	public abstract Optional<String> query();

	public abstract Optional<String> queryFile();

	public abstract Optional<String> readPreference();

	@Value.Default
	public boolean forceTableScan() {
		return false;
	}

	public abstract Optional<String> archive();

	@Value.Default
	public boolean dumpDbUsersAndRoles() {
		return false;
	}

	@Value.Default
	public boolean gzip() {
		return false;
	}

	@Value.Default
	public boolean repair() {
		return false;
	}

	public abstract Optional<String> dir();

	@Value.Default
	public boolean isOplog() {
		return false;
	}

	public abstract Optional<String> excludeCollection();

	public abstract Optional<String> excludeCollectionWithPrefix();

	public abstract OptionalInt numberOfParallelCollections();

	@Override
	@Value.Auxiliary
	public List<String> asArguments(ServerAddress serverAddress) {
		return getCommandLine(this,serverAddress);
	}

	public static ImmutableMongoDumpArguments.Builder builder() {
		return ImmutableMongoDumpArguments.builder();
	}

	public static ImmutableMongoDumpArguments defaults() {
		return builder().build();
	}

	private static List<String> getCommandLine(MongoDumpArguments config, ServerAddress serverAddress) {
		Arguments.Builder builder = Arguments.builder();

		builder.addIf(config.verbose(),"-v");
		builder.add("--port",""+serverAddress.getPort());
		builder.add("--host", serverAddress.getHost());

		builder.addIf("--db", config.databaseName());
		builder.addIf("--collection", config.collectionName());
		builder.addIf("--query", config.query());

		builder.addIf("--queryFile", config.queryFile());
		builder.addIf("--readPreference", config.readPreference());
		builder.addIf(config.forceTableScan(),"--forceTableScan");

		config.archive().ifPresent(it -> builder.add("--archive="+it));

		builder.addIf(config.dumpDbUsersAndRoles(),"--dumpDbUsersAndRoles");

		builder.addIf("--out", config.dir());
		builder.addIf(config.gzip(),"--gzip");
		builder.addIf(config.repair(),"--repair");
		builder.addIf(config.isOplog(),"--oplog");

		builder.addIf("--excludeCollection", config.excludeCollection());
		builder.addIf("--excludeCollectionWithPrefix", config.excludeCollectionWithPrefix());
		config.numberOfParallelCollections().ifPresent(it -> builder.add("--numParallelCollections", ""+it));

		return builder.build();
	}
}
