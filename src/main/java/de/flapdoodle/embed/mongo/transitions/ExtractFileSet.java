package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.process.archives.ExtractedFileSet;
import de.flapdoodle.embed.process.config.store.Package;
import de.flapdoodle.embed.process.nio.directories.PersistentDir;
import de.flapdoodle.embed.process.store.ContentHashExtractedFileSetStore;
import de.flapdoodle.embed.process.store.DownloadCache;
import de.flapdoodle.embed.process.store.ExtractedFileSetStore;
import de.flapdoodle.embed.process.store.LocalDownloadCache;
import de.flapdoodle.embed.process.transitions.DownloadPackage;
import de.flapdoodle.embed.process.transitions.ExtractPackage;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.reverse.Transitions;
import de.flapdoodle.reverse.transitions.Derive;
import de.flapdoodle.reverse.transitions.Start;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

public interface ExtractFileSet {

	default Map<String, String> systemEnv() {
		return System.getenv();
	}

	default Transition<PersistentDir> persistentBaseDir() {
		return Start.to(PersistentDir.class)
			.providedBy(() -> Optional.ofNullable(systemEnv().get("EMBEDDED_MONGO_ARTIFACTS"))
				.map(Paths::get)
				.map(PersistentDir::of)
				.orElseGet(PersistentDir.userHome(".embedmongo")));
	}

	default Transition<DownloadCache> downloadCache() {
		return Derive.given(PersistentDir.class)
			.state(DownloadCache.class)
			.deriveBy(storeBaseDir -> new LocalDownloadCache(storeBaseDir.value().resolve("archives")))
			.withTransitionLabel("downloadCache");
	}

	default Transition<ExtractedFileSetStore> extractedFileSetStore() {
		return Derive.given(PersistentDir.class)
			.state(ExtractedFileSetStore.class)
			.deriveBy(baseDir -> new ContentHashExtractedFileSetStore(baseDir.value().resolve("fileSets")))
			.withTransitionLabel("extractedFileSetStore");
	}

	default DownloadPackage downloadPackage() {
		return DownloadPackage.withDefaults();
	}

	default Transition<ExtractedFileSet> extractPackage() {
		return ExtractPackage.withDefaults()
			.withExtractedFileSetStore(StateID.of(ExtractedFileSetStore.class));
	}

	default Transition<Package> packageOfDistribution() {
		return PackageOfCommandDistribution.withDefaults();
	}

	default Transitions extractFileSet() {
		return Transitions.from(
			persistentBaseDir(),
			downloadCache(),
			packageOfDistribution(),
			downloadPackage(),
			extractedFileSetStore(),
			extractPackage()
		);
	}
}
