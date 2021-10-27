package de.flapdoodle.embed.mongo.packageresolver;

import de.flapdoodle.embed.mongo.distribution.NumericVersion;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Version;
import org.immutables.value.Value;

@Value.Immutable
public abstract class VersionRange implements DistributionMatch {
  @Value.Parameter
  abstract NumericVersion min();
  @Value.Parameter
  abstract NumericVersion max();

  @Override
  public boolean match(Distribution distribution) {
    Version version = distribution.version();
    NumericVersion asNumeric = NumericVersion.of(version.asInDownloadPath());
    return min().isOlderOrEqual(asNumeric) && asNumeric.isOlderOrEqual(max());
  }

  public static VersionRange of(NumericVersion min, NumericVersion max) {
    return ImmutableVersionRange.of(min, max);
  }

  public static VersionRange of(String min, String max) {
    return of(NumericVersion.of(min), NumericVersion.of(max));
  }
}
