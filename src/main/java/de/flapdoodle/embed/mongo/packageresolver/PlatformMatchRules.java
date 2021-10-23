package de.flapdoodle.embed.mongo.packageresolver;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public abstract class PlatformMatchRules {
  abstract List<PlatformMatchRule> rules();

  public ImmutablePlatformMatchRules with(PlatformMatchRule rule) {
    return ImmutablePlatformMatchRules.builder().rules(rules()).addRules(rule).build();
  }

  static ImmutablePlatformMatchRules empty() {
    return ImmutablePlatformMatchRules.builder().build();
  }
}
