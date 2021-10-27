package de.flapdoodle.embed.mongo.packageresolver;

import de.flapdoodle.embed.process.config.store.PackageResolver;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(stagedBuilder = true)
public interface PlatformMatchRule {
  DistributionMatch match();
  PackageResolver resolver();

  static PlatformMatchRule of(PlatformMatch match, PackageResolver resolver) {
    return builder()
            .match(match)
            .resolver(resolver)
            .build();
  }

  static ImmutablePlatformMatchRule.MatchBuildStage builder() {
    return ImmutablePlatformMatchRule.builder();
  }
}
