/**
 * Copyright (C) 2011
 *   Michael Mosmann <michael@mosmann.de>
 *   Martin Jöhren <m.joehren@googlemail.com>
 *
 * with contributions from
 * 	konstantin-ba@github,Archimedes Trajano	(trajano@github)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.flapdoodle.embed.mongo.commands;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.Storage;
import de.flapdoodle.embed.mongo.config.SupportConfig;
import de.flapdoodle.embed.mongo.distribution.Feature;
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
import de.flapdoodle.embed.mongo.types.DatabaseDir;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.runtime.NUMA;
import de.flapdoodle.os.OS;
import de.flapdoodle.os.Platform;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;

import static java.lang.String.format;

@Value.Immutable
public abstract class MongodArguments {
	private static final Logger LOGGER = LoggerFactory.getLogger(MongodArguments.class);

	@Value.Default
	public int syncDelay() {
		return 0;
	}

	@Value.Default
	public boolean useDefaultSyncDelay() {
		return false;
	}

	public abstract Optional<String> storageEngine();

	@Value.Default
	public boolean isVerbose() {
		return false;
	}

	@Value.Default
	public boolean useNoPrealloc() {
		return true;
	}

	@Value.Default
	public boolean useSmallFiles() {
		return true;
	}

	@Value.Default
	public boolean useNoJournal() {
		return true;
	}

	@Value.Default
	public boolean enableTextSearch() {
		return false;
	}

	@Value.Default
	public boolean auth() {
		return false;
	}

	@Value.Default
	public boolean master() {
		return false;
	}

	@Value.Default
	public Storage replication() {
		return new Storage();
	}

	@Value.Default
	public boolean isConfigServer() {
		return false;
	}

	@Value.Default
	public boolean isShardServer() {
		return false;
	}

	public abstract Map<String, String> params();

	public abstract Map<String, String> args();

	@Value.Auxiliary
	public List<String> asArguments(
		Platform platform,
		IFeatureAwareVersion version,
		Net net,
		Path executable,
		DatabaseDir dbDirectory
	) {
		return warpWithNumaSupport(platform, getCommandLine(this, version, net, executable, dbDirectory.value()));
	}

	public static ImmutableMongodArguments.Builder builder() {
		return ImmutableMongodArguments.builder();
	}

	public static ImmutableMongodArguments defaults() {
		return builder().build();
	}

	private static List<String> getCommandLine(
		MongodArguments config,
		IFeatureAwareVersion version,
		Net net,
		Path executable,
		Path dbDirectory
	) {
		Arguments.Builder builder = Arguments.builder();

		builder.add(/*executable.toAbsolutePath().toString(),*/ "--dbpath", dbDirectory.toAbsolutePath().toString());

		config.params().forEach((key, val) -> builder.add("--setParameter", format("%s=%s", key, val)));
		config.args().forEach(builder::add);

		builder.add(config.auth() ? "--auth" : "--noauth");

		builder.addIf(!version.enabled(Feature.DISABLE_USE_PREALLOC) && config.useNoPrealloc(), "--noprealloc");
		builder.addIf(!version.enabled(Feature.DISABLE_USE_SMALL_FILES) && config.useSmallFiles(), "--smallfiles");
		builder.addIf(config.useNoJournal() && !config.isConfigServer(),"--nojournal");
		builder.addIf(config.master(),"--master");

		if (config.storageEngine().isPresent()) {
			builder.addIf(version.enabled(Feature.STORAGE_ENGINE), "--storageEngine", config.storageEngine().get());
		}

		builder.addIf(config.isVerbose(),"-v");

		builder.addIf(!version.enabled(Feature.NO_HTTP_INTERFACE_ARG),"--nohttpinterface");

		builder.add("--port");
		builder.add("" + net.getPort());
		builder.addIf(net.isIpv6(), "--ipv6");

		String bindIp = net.getBindIp();
		builder.addIf(bindIp!=null, "--bind_ip", Objects.equals("localhost", bindIp) && version.enabled(Feature.NO_BIND_IP_TO_LOCALHOST) ? "127.0.0.1" : bindIp);

		Storage replication = config.replication();
		builder.addIf(replication.getReplSetName() != null,"--replSet",replication.getReplSetName());
		builder.addIf(replication.getOplogSize() != 0,"--oplogSize",String.valueOf(replication.getOplogSize()));

		builder.addIf(config.isConfigServer(),"--configsvr");
		builder.addIf(config.isShardServer(),"--shardsvr");
		builder.addIf(version.enabled(Feature.SYNC_DELAY) && !config.useDefaultSyncDelay(),"--syncdelay=" + config.syncDelay());
		builder.addIf(version.enabled(Feature.TEXT_SEARCH) && config.enableTextSearch(),"--setParameter","textSearchEnabled=true");

		return builder.build();
	}

	private static List<String> warpWithNumaSupport(Platform platform, List<String> commands) {
		if (NUMA.isNUMA(new SupportConfig(Command.MongoD), platform)) {
			if (platform.operatingSystem() == OS.Linux) {
				List<String> ret = new ArrayList<>();
				ret.add("numactl");
				ret.add("--interleave=all");
				ret.addAll(commands);
				return Collections.unmodifiableList(ret);
			} else {
				LOGGER.warn("NUMA Plattform detected, but not supported.");
			}
		}
		return commands;
	}
}
