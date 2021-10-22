package de.flapdoodle.embed.mongo.packageresolver;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public abstract class PlatformMatchRules {
  abstract List<PlatformMatchRule> rules();

  static ImmutablePlatformMatchRules empty() {
    return ImmutablePlatformMatchRules.builder().build();
  }
}
