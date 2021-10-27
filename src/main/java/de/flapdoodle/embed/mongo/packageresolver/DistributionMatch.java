package de.flapdoodle.embed.mongo.packageresolver;

import java.util.Arrays;

public interface DistributionMatch {
  boolean match(de.flapdoodle.embed.process.distribution.Distribution distribution);

  default DistributionMatch andThen(DistributionMatch other) {
    DistributionMatch that = this;
    return dist -> that.match(dist) && other.match(dist);
  }

  static DistributionMatch all() {
    return __ -> true;
  }

  static DistributionMatch any(DistributionMatch ... matcher) {
    return dist -> Arrays.stream(matcher).anyMatch(m -> m.match(dist));
  }
}
