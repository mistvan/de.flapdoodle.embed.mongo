package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.checks.Preconditions;
import de.flapdoodle.embed.mongo.commands.MongodArguments;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
import de.flapdoodle.embed.process.archives.ExtractedFileSet;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.embed.process.nio.directories.TempDir;
import de.flapdoodle.embed.process.types.ProcessArguments;
import de.flapdoodle.os.Platform;
import de.flapdoodle.reverse.State;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.StateLookup;
import de.flapdoodle.reverse.Transition;
import org.immutables.value.Value;

import java.util.List;
import java.util.Set;

@Value.Immutable
public abstract class MongodProcessArguments implements CommandProcessArguments<MongodArguments> {

	@Override
	@Value.Default
	public StateID<ProcessArguments> destination() {
		return StateID.of(ProcessArguments.class);
	}

	@Override
	@Value.Default
	public StateID<MongodArguments> arguments() {
		return StateID.of(MongodArguments.class);
	}

	@Value.Default
	public StateID<Platform> platform() {
		return StateID.of(Platform.class);
	}

	@Value.Default
	public StateID<Version> version() {
		return StateID.of(Version.class);
	}

	@Value.Default
	public StateID<Net> net() {
		return StateID.of(Net.class);
	}

	@Value.Default
	public StateID<ExtractedFileSet> processExecutable() {
		return StateID.of(ExtractedFileSet.class);
	}

	@Value.Default
	public StateID<TempDir> tempDir() {
		return StateID.of(TempDir.class);
	}

	@Override
	@Value.Auxiliary
	public Set<StateID<?>> sources() {
		return StateID.setOf(arguments(), platform(), version(), net(), processExecutable(), tempDir());
	}

	@Override
	public State<ProcessArguments> result(StateLookup lookup) {
		MongodArguments arguments = lookup.of(arguments());
		Platform platform = lookup.of(platform());
		Version version = lookup.of(version());
		Preconditions.checkArgument(version instanceof IFeatureAwareVersion,"invalid type: %s does not implement %s",version, IFeatureAwareVersion.class);
		IFeatureAwareVersion featureAwareVersion = (IFeatureAwareVersion) version;
		Net net = lookup.of(net());
		ExtractedFileSet extractedFileSet=lookup.of(processExecutable());
		TempDir tempDir=lookup.of(tempDir());

		List<String> commandLine = arguments.asArguments(platform, featureAwareVersion, net, extractedFileSet.executable(), tempDir.value());
		return State.of(ProcessArguments.of(commandLine));
	}

	public static ImmutableMongodProcessArguments withDefaults() {
		return builder().build();
	}

	public static ImmutableMongodProcessArguments.Builder builder() {
		return ImmutableMongodProcessArguments.builder();
	}
}
