package de.flapdoodle.embed.mongo.commands;

import de.flapdoodle.checks.Preconditions;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Feature;
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
import de.flapdoodle.os.Platform;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
public abstract class MongosArguments {

	@Value.Default
	public boolean verbose() {
		return false;
	}

	public abstract Optional<String> configDB();

	public abstract Optional<String> replicaSet();

	@Value.Auxiliary
	public List<String> asArguments(
		Platform platform,
		IFeatureAwareVersion version,
		Net net
	) {
		return getCommandLine(this, version, net);
	}

	public static ImmutableMongosArguments.Builder builder() {
		return ImmutableMongosArguments.builder();
	}

	public static ImmutableMongosArguments defaults() {
		return builder().build();
	}

	private static List<String> getCommandLine(
		MongosArguments config,
		IFeatureAwareVersion version,
		Net net
	) {
		Arguments.Builder ret = Arguments.builder();

		ret.addIf(!version.enabled(Feature.NO_CHUNKSIZE_ARG),"--chunkSize","1");
		ret.addIf(config.verbose(),"-v");
		ret.addIf(!version.enabled(Feature.NO_HTTP_INTERFACE_ARG),"--nohttpinterface");

		ret.add("--port");
		ret.add("" + net.getPort());
		ret.addIf(net.isIpv6(), "--ipv6");

		if (config.configDB().isPresent()) {
			ret.add("--configdb");
			if (version.enabled(Feature.MONGOS_CONFIGDB_SET_STYLE)) {
				Preconditions.checkArgument(config.replicaSet().isPresent(),"you must define a replicaSet");
				ret.add(config.replicaSet().get()+"/"+config.configDB().get());
			} else {
				ret.add(config.configDB().get());
			}
		}

		return ret.build();
	}

}
