package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.mongo.packageresolver.Command;
import de.flapdoodle.embed.mongo.types.DistributionBaseUrl;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.nio.directories.PersistentDir;
import de.flapdoodle.embed.process.nio.directories.TempDir;
import de.flapdoodle.embed.process.store.ContentHashExtractedFileSetStore;
import de.flapdoodle.embed.process.store.LocalDownloadCache;
import de.flapdoodle.embed.process.transitions.DownloadPackage;
import de.flapdoodle.embed.process.transitions.ExtractPackage;
import de.flapdoodle.embed.process.types.Name;
import de.flapdoodle.reverse.*;
import de.flapdoodle.reverse.transitions.Derive;

public interface ExtractedFileSetFor {

	default PersistentDir storeBaseDir() {
		return PersistentDir.userHome(".embedmongo").get();
	}

	default LocalDownloadCache downloadCache() {
		return new LocalDownloadCache(storeBaseDir().value().resolve("archives"));
	}

	default ContentHashExtractedFileSetStore extractedFileSetStore() {
		return new ContentHashExtractedFileSetStore(storeBaseDir().value().resolve("fileSets"));
	}

	default DownloadPackage downloadPackage() {
		return DownloadPackage.with(downloadCache());
	}

	default ExtractPackage extractPackage() {
		return ExtractPackage.withDefaults()
			.withExtractedFileSetStore(extractedFileSetStore());
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
			Derive.given(localCommandStateID).state(Name.class).deriveBy(c -> Name.of(c.commandName())).withTransitionLabel("name from command"),

			PackageOfCommandDistribution.withDefaults()
				.withDistributionBaseUrl(distributionBaseUrlStateID),

			downloadPackage(),
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
