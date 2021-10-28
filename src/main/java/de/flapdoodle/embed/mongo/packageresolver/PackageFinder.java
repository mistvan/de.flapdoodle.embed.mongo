package de.flapdoodle.embed.mongo.packageresolver;

import de.flapdoodle.embed.process.config.store.DistributionPackage;
import de.flapdoodle.embed.process.distribution.Distribution;

import java.util.Optional;

public interface PackageFinder {
  Optional<DistributionPackage> packageFor(Distribution distribution);
}
