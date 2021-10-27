package de.flapdoodle.embed.mongo.packageresolver;

import de.flapdoodle.embed.process.config.store.DistributionPackage;
import de.flapdoodle.embed.process.distribution.Distribution;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
public abstract class PlatformMatchRules {
  abstract List<PlatformMatchRule> rules();

  public ImmutablePlatformMatchRules with(PlatformMatchRule rule) {
    return ImmutablePlatformMatchRules.builder().rules(rules()).addRules(rule).build();
  }

  @Value.Auxiliary
  public Optional<DistributionPackage> packageFor(Distribution distribution) {
    for (PlatformMatchRule rule : rules()) {
      if (rule.match().match(distribution)) {
        return Optional.of(rule.resolver().packageFor(distribution));
      }
    }
    return Optional.empty();
  }


  static ImmutablePlatformMatchRules empty() {
    return ImmutablePlatformMatchRules.builder().build();
  }
}
