package de.flapdoodle.embed.mongo.commands;

import com.mongodb.ServerAddress;
import de.flapdoodle.embed.mongo.config.MongoShellConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.process.extract.ExtractedFileSet;
import org.immutables.value.Value;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;

@Value.Immutable
public abstract class MongoShellArguments implements MongoToolsArguments {
	public abstract List<String> scriptParameters();

	public abstract Optional<String> scriptName();

	public abstract Optional<String> dbName();

	public abstract Optional<String> password();

	public abstract Optional<String> userName();

	@Override
	public List<String> asArguments(ServerAddress serverAddress) {
		return getCommandLine(this, serverAddress);
	}

	public static ImmutableMongoShellArguments.Builder builder() {
		return ImmutableMongoShellArguments.builder();
	}

	public static MongoShellArguments defaults() {
		return builder().build();
	}
	
	private static List<String> getCommandLine(
		MongoShellArguments config,
		ServerAddress serverAddress
	)
		 {
		Arguments.Builder ret = Arguments.builder();

		String hostname=serverAddress.getHost();
		int port = serverAddress.getPort();

		ret.addIf("--username", config.userName());
		ret.addIf("--password", config.password());

		if (config.dbName().isPresent()) {
			 ret.add(hostname+":"+port+"/"+config.dbName().get());
		} else {
			ret.add(hostname+":"+port);
		}

		if (!config.scriptParameters().isEmpty()) {
			ret.add("--eval");
			StringBuilder eval = new StringBuilder();
			for (String parameter : config.scriptParameters()) {
				eval.append(parameter).append("; ");
			}
			ret.add(eval.toString());
		}
		if (config.scriptName().isPresent()) {
			ret.add(config.scriptName().get());
		}

		return ret.build();
	}

}
