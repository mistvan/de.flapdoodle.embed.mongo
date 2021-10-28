package de.flapdoodle.embed.mongo.packageresolver;

import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.os.Architecture;
import de.flapdoodle.os.CommonArchitecture;
import de.flapdoodle.os.ImmutablePlatform;
import de.flapdoodle.os.OS;
import de.flapdoodle.os.linux.UbuntuVersion;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PlatformMatchTest {

  @Test
  void differentVersionsMustNotMatch() {
    ImmutablePlatform platform = ImmutablePlatform.builder()
            .operatingSystem(OS.Linux)
            .architecture(CommonArchitecture.X86_64)
            .build();
    
    boolean result = PlatformMatch.any().withVersion(UbuntuVersion.UBUNTU_18_04)
            .match(Distribution.of(Version.of("1.2.3"), platform));

    assertThat(result).isFalse();
  }

  @Test
  void sameVersionMustNotMatch() {
    ImmutablePlatform platform = ImmutablePlatform.builder()
            .operatingSystem(OS.Linux)
            .architecture(CommonArchitecture.X86_64)
            .version(UbuntuVersion.UBUNTU_18_04)
            .build();

    boolean result = PlatformMatch.any().withVersion(UbuntuVersion.UBUNTU_18_04, UbuntuVersion.UBUNTU_19_04)
            .match(Distribution.of(Version.of("1.2.3"), platform));

    assertThat(result).isTrue();
  }

  @Test
  void noVersionMustNotMatch() {
    ImmutablePlatform platform = ImmutablePlatform.builder()
            .operatingSystem(OS.Linux)
            .architecture(CommonArchitecture.X86_64)
            .version(UbuntuVersion.UBUNTU_18_04)
            .build();

    boolean result = PlatformMatch.any()
            .match(Distribution.of(Version.of("1.2.3"), platform));

    assertThat(result).isTrue();
  }
}