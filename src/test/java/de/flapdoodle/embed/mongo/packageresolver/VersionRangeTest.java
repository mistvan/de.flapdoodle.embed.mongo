package de.flapdoodle.embed.mongo.packageresolver;

import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.os.Platform;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class VersionRangeTest {

  @Test
  public void mustMatchVersionRange() {
    VersionRange range = VersionRange.of("4.0.0", "4.0.26");
    boolean result = range.match(Distribution.of(Version.of("4.0.12"), Platform.detect()));
    assertThat(result).isTrue();
  }
}