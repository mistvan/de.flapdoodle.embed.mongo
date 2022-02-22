package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.mongo.types.DistributionBaseUrl;
import de.flapdoodle.embed.process.transitions.InitTempDirectory;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.reverse.Transitions;
import de.flapdoodle.reverse.transitions.Start;
import org.immutables.value.Value;

public interface WorkspaceDefaults {
	default InitTempDirectory initTempDirectory() {
		return InitTempDirectory.withPlatformTempRandomSubDir();
	}

	default Transition<DistributionBaseUrl> distributionBaseUrl() {
		return Start.to(DistributionBaseUrl.class)
			.initializedWith(DistributionBaseUrl.of("https://fastdl.mongodb.org"));
	}

	default Transitions workspaceDefaults() {
		return Transitions.from(initTempDirectory(), distributionBaseUrl());
	}
}
