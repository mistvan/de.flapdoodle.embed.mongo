package de.flapdoodle.embed.mongo.packageresolver;

import de.flapdoodle.embed.process.config.store.DistributionPackage;
import de.flapdoodle.embed.process.config.store.PackageResolver;
import de.flapdoodle.os.Distribution;
import org.immutables.value.Value;

import java.util.function.Function;

@Value.Immutable
public interface PlatformMatchRule {
  PlatformMatch match();
  PackageResolver resolver();

  static PlatformMatchRule of(PlatformMatch match, PackageResolver resolver) {
    return ImmutablePlatformMatchRule.builder()
            .match(match)
            .resolver(resolver)
            .build();
  }
}
