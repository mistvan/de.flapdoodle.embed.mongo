package de.flapdoodle.embed.mongo.packageresolver.linux;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.packageresolver.HtmlParserResultTester;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.os.CommonArchitecture;
import de.flapdoodle.os.ImmutablePlatform;
import de.flapdoodle.os.OS;
import de.flapdoodle.os.Platform;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LinuxPackageResolverTest {

  /*
    Linux (legacy) undefined
    https://fastdl.mongodb.org/linux/mongodb-linux-i686-{}.tgz
    3.2.21 - 3.2.0, 3.0.14 - 3.0.0, 2.6.12 - 2.6.0
  */
  @ParameterizedTest
  @ValueSource(strings = {"3.2.21 - 3.2.0", "3.0.14 - 3.0.0", "2.6.12 - 2.6.0"})
  public void legacy32Bit(String version) {
    assertThat(linuxWith(CommonArchitecture.X86_32), version)
            .resolvesTo("/linux/mongodb-linux-i686-{}.tgz");
  }

  /*
    Linux (legacy) x64
    https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-{}.tgz
    4.0.26 - 4.0.0, 3.6.22 - 3.6.0, 3.4.23 - 3.4.9, 3.4.7 - 3.4.0, 3.2.21 - 3.2.0, 3.0.14 - 3.0.0, 2.6.12 - 2.6.0
  */
  @ParameterizedTest
  @ValueSource(strings = {"4.0.26 - 4.0.0", "3.6.22 - 3.6.0", "3.4.23 - 3.4.9", "3.4.7 - 3.4.0", "3.2.21 - 3.2.0", "3.0.14 - 3.0.0", "2.6.12 - 2.6.0"})
  public void  legacy64Bit(String version) {
    assertThat(linuxWith(CommonArchitecture.X86_64), version)
            .resolvesTo("/linux/mongodb-linux-x86_64-{}.tgz");
  }


  private static Platform linuxWith(CommonArchitecture architecture) {
    return ImmutablePlatform.builder()
            .operatingSystem(OS.Linux)
            .architecture(architecture)
            .build();
  }

  private static HtmlParserResultTester assertThat(Platform platform, String versionList) {
    return HtmlParserResultTester.with(
            new LinuxPackageResolver(Command.Mongo),
            version -> Distribution.of(Version.of(version), platform),
            versionList);
  }

}