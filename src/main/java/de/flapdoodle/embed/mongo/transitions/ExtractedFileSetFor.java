package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.mongo.packageresolver.Command;
import de.flapdoodle.embed.mongo.types.DistributionBaseUrl;
import de.flapdoodle.embed.process.archives.ExtractedFileSet;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.nio.directories.PersistentDir;
import de.flapdoodle.embed.process.nio.directories.TempDir;
import de.flapdoodle.embed.process.store.ContentHashExtractedFileSetStore;
import de.flapdoodle.embed.process.store.DownloadCache;
import de.flapdoodle.embed.process.store.ExtractedFileSetStore;
import de.flapdoodle.embed.process.store.LocalDownloadCache;
import de.flapdoodle.embed.process.transitions.DownloadPackage;
import de.flapdoodle.embed.process.transitions.ExtractPackage;
import de.flapdoodle.embed.process.types.Name;
import de.flapdoodle.reverse.*;
import de.flapdoodle.reverse.transitions.Derive;
import de.flapdoodle.reverse.transitions.Start;

public interface ExtractedFileSetFor {

	default Transition<PersistentDir> storeBase() {
		return Start.to(PersistentDir.class)
			.providedBy(PersistentDir.userHome(".embedmongo"));
	}

	default Transition<DownloadCache> downloadCache() {
		return Derive.given(PersistentDir.class)
			.state(DownloadCache.class)
			.deriveBy(storeBaseDir -> new LocalDownloadCache(storeBaseDir.value().resolve("archives")));
	}

	default Transition<ExtractedFileSetStore> extractedFileSetStore() {
		return Derive.given(PersistentDir.class)
			.state(ExtractedFileSetStore.class)
			.deriveBy(baseDir -> new ContentHashExtractedFileSetStore(baseDir.value().resolve("fileSets")));
	}

	default DownloadPackage downloadPackage() {
		return DownloadPackage.withDefaults();
	}

	default Transition<ExtractedFileSet> extractPackage() {
		return ExtractPackage.withDefaults()
			.withExtractedFileSetStore(StateID.of(ExtractedFileSetStore.class));
	}

	default Transition<de.flapdoodle.embed.process.archives.ExtractedFileSet> extractedFileSetFor(
		StateID<de.flapdoodle.embed.process.archives.ExtractedFileSet> destination,
		StateID<Distribution> distributionStateID,
		StateID<TempDir> tempDirStateID,
		StateID<Command> commandStateID,
		StateID<DistributionBaseUrl> distributionBaseUrlStateID
	) {
		StateID<Distribution> localDistributionStateID = StateID.of(Distribution.class);
		StateID<TempDir> localTempDirStateID = StateID.of(TempDir.class);
		StateID<Command> localCommandStateID = StateID.of(Command.class);

		Transitions transitions = Transitions.from(
			storeBase(),
			downloadCache(),
			
			Derive.given(localCommandStateID).state(Name.class).deriveBy(c -> Name.of(c.commandName())).withTransitionLabel("name from command"),

			PackageOfCommandDistribution.withDefaults()
				.withDistributionBaseUrl(distributionBaseUrlStateID),

			downloadPackage(),
			extractedFileSetStore(),
			extractPackage()
		);

		return transitions.walker()
			.asTransitionTo(
				TransitionMapping.builder("extract file set", StateMapping.of(StateID.of(de.flapdoodle.embed.process.archives.ExtractedFileSet.class), destination))
					.addMappings(StateMapping.of(distributionStateID, localDistributionStateID))
					.addMappings(StateMapping.of(tempDirStateID, localTempDirStateID))
					.addMappings(StateMapping.of(commandStateID, localCommandStateID))
					.build());
	}

}
