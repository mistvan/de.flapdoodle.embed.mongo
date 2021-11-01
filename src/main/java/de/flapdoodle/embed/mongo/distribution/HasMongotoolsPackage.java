package de.flapdoodle.embed.mongo.distribution;

import java.util.Optional;

public interface HasMongotoolsPackage {
		Optional<MongotoolsVersion.Main> mongotoolsVersion();
}
