package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.os.Platform;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.reverse.Transitions;
import de.flapdoodle.reverse.transitions.Join;
import de.flapdoodle.reverse.transitions.Start;
import org.immutables.value.Value;

public interface VersionAndPlatform {
	default Transition<Platform> platform() {
		return Start.to(Platform.class).providedBy(Platform::detect);
	}

	default Transition<Distribution> distribution() {
		return Join.given(de.flapdoodle.embed.process.distribution.Version.class).and(Platform.class).state(Distribution.class)
			.deriveBy(Distribution::of)
			.withTransitionLabel("version + platform");
	}

	default Transitions versionAndPlatform() {
		return Transitions.from(
			platform(),
			distribution()
		);
	}
}
