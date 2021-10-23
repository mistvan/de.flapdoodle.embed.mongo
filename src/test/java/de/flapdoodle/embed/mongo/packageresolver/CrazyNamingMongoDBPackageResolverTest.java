package de.flapdoodle.embed.mongo.packageresolver;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.TestUtils;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.store.DistributionPackage;
import de.flapdoodle.os.CommonArchitecture;
import de.flapdoodle.os.OS;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CrazyNamingMongoDBPackageResolverTest {

  @Test
  public void matchKnownVersions() {
    CrazyNamingMongoDBPackageResolver testee = new CrazyNamingMongoDBPackageResolver(Command.Mongo, distribution -> {
      throw new IllegalArgumentException("should not be called");
    });

    DistributionPackage result = testee.packageFor(TestUtils.distributionOf(Version.V4_2_13, OS.Windows, CommonArchitecture.X86_64));
    assertThat(result.archivePath()).isEqualTo("https://fastdl.mongodb.org/win32/mongodb-win32-x86_64-2012plus-4.2.13.zip");
  }
}