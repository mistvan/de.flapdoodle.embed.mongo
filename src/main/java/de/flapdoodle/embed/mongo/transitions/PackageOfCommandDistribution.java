package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.packageresolver.PlatformPackageResolver;
import de.flapdoodle.embed.mongo.types.DistributionBaseUrl;
import de.flapdoodle.embed.process.archives.ArchiveType;
import de.flapdoodle.embed.process.config.store.DistributionPackage;
import de.flapdoodle.embed.process.config.store.Package;
import de.flapdoodle.embed.process.config.store.PackageResolver;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.reverse.State;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.StateLookup;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.reverse.naming.HasLabel;
import org.immutables.value.Value;

import java.util.Set;
import java.util.function.Function;

@Value.Immutable
public abstract class PackageOfCommandDistribution implements Transition<Package>, HasLabel {

	@Override public String transitionLabel() {
		return "Package of Command-Distribution";
	}

	@Value.Default
	protected Function<Command, PackageResolver> legacyPackageResolverFactory() {
		return PlatformPackageResolver::new;
	}

	@Value.Auxiliary
	protected Package packageOf(Command command, Distribution distribution, DistributionBaseUrl baseUrl) {
		DistributionPackage distPackage = legacyPackageResolverFactory().apply(command).packageFor(distribution);
		return Package.of(archiveTypeOfLegacy(distPackage.archiveType()), distPackage.fileSet(),  baseUrl.value() /*"https://fastdl.mongodb.org"*/+distPackage.archivePath());
	}

	private static ArchiveType archiveTypeOfLegacy(de.flapdoodle.embed.process.distribution.ArchiveType archiveType) {
		switch (archiveType) {
			case EXE:
				return ArchiveType.EXE;
			case TBZ2:
				return ArchiveType.TBZ2;
			case TGZ:
				return ArchiveType.TGZ;
			case ZIP:
				return ArchiveType.ZIP;
			case TXZ:
				return ArchiveType.TXZ;
		}
		throw new IllegalArgumentException("Could not map: "+archiveType);
	}

	@Value.Default
	public StateID<Command> command() {
		return StateID.of(Command.class);
	}

	@Value.Default
	public StateID<Distribution> distribution() {
		return StateID.of(Distribution.class);
	}

	@Value.Default
	public StateID<DistributionBaseUrl> distributionBaseUrl() {
		return StateID.of(DistributionBaseUrl.class);
	}

	@Override
	@Value.Default
	public StateID<Package> destination() {
		return StateID.of(Package.class);
	}

	@Override
	public Set<StateID<?>> sources() {
		return StateID.setOf(command(), distribution(), distributionBaseUrl());
	}

	@Override
	public State<Package> result(StateLookup lookup) {
		Command command = lookup.of(command());
		Distribution distribution = lookup.of(distribution());
		DistributionBaseUrl baseUrl = lookup.of(distributionBaseUrl());
		return State.of(packageOf(command,distribution, baseUrl));
	}

	public static ImmutablePackageOfCommandDistribution.Builder builder() {
		return ImmutablePackageOfCommandDistribution.builder();
	}

	public static ImmutablePackageOfCommandDistribution withDefaults() {
		return builder().build();
	}
}
