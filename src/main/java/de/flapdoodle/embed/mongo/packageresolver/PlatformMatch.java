package de.flapdoodle.embed.mongo.packageresolver;

import de.flapdoodle.os.*;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public interface PlatformMatch {
  Optional<Distribution> distribution();
  Optional<Version> version();
  Optional<CPUType> cpuType();
  Optional<BitSize> bitSize();

  static ImmutablePlatformMatch.Builder builder() {
    return ImmutablePlatformMatch.builder();
  }

  static ImmutablePlatformMatch any() {
    return builder().build();
  }
}
