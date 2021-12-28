package de.flapdoodle.embed.mongo.packageresolver.linux;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.packageresolver.HtmlParserResultTester;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.os.CommonArchitecture;
import de.flapdoodle.os.ImmutablePlatform;
import de.flapdoodle.os.OS;
import de.flapdoodle.os.Platform;
import de.flapdoodle.os.linux.AmazonVersion;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AmazonPackageResolverTest {
	/*
	 * Amazon Linux 2 ARM 64
	 * https://fastdl.mongodb.org/linux/mongodb-linux-aarch64-amazon2-{}.tgz
	 * 5.0.2 - 5.0.0, 4.4.9 - 4.4.4, 4.2.16 - 4.2.13
	 */
	@ParameterizedTest
	@ValueSource(strings = {"5.0.2 - 5.0.0", "4.4.9 - 4.4.4", "4.2.16 - 4.2.13"})
	public void amazon2LinuxArm(String version) {
		assertThat(linuxWith(CommonArchitecture.ARM_64, AmazonVersion.AmazonLinux2), version)
			.resolvesTo("/linux/mongodb-linux-aarch64-amazon2-{}.tgz");
	}

	/*
	 * Amazon Linux 2 x64
	 * https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-amazon2-{}.tgz
	 * 5.0.2 - 5.0.0, 4.4.9 - 4.4.0, 4.2.16 - 4.2.5, 4.2.3 - 4.2.0, 4.0.26 - 4.0.0, 3.6.22
	 */
	@ParameterizedTest
	@ValueSource(strings = {"5.0.2 - 5.0.0", "4.4.9 - 4.4.0", "4.2.16 - 4.2.5", "4.2.3 - 4.2.0", "4.0.26 - 4.0.0", "3.6.22"})
	public void amazon2Linux(String version) {
		assertThat(linuxWith(CommonArchitecture.X86_64, AmazonVersion.AmazonLinux2), version)
			.resolvesTo("/linux/mongodb-linux-x86_64-amazon2-{}.tgz");
	}

	/*
	 * Amazon Linux x64
	 * https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-amazon-{}.tgz
	 * 5.0.2 - 5.0.0, 4.4.9 - 4.4.0, 4.2.16 - 4.2.5, 4.2.3 - 4.2.0, 4.0.26 - 4.0.0, 3.6.22 - 3.6.0, 3.4.23 - 3.4.9, 3.4.7 - 3.4.0, 3.2.21 - 3.2.0, 3.0.14 - 3.0.0
	 */
	@ParameterizedTest
	@ValueSource(strings = {"5.0.2 - 5.0.0", "4.4.9 - 4.4.0", "4.2.16 - 4.2.5", "4.2.3 - 4.2.0", "4.0.26 - 4.0.0", "3.6.22 - 3.6.0", "3.4.23 - 3.4.9", "3.4.7 - 3.4.0", "3.2.21 - 3.2.0", "3.0.14 - 3.0.0"})
	public void amazonLinux(String version) {
		assertThat(linuxWith(CommonArchitecture.X86_64, AmazonVersion.AmazonLinux), version)
			.resolvesTo("/linux/mongodb-linux-x86_64-amazon-{}.tgz");
	}

	private static Platform linuxWith(CommonArchitecture architecture, de.flapdoodle.os.Version version) {
		return ImmutablePlatform.builder()
			.operatingSystem(OS.Linux)
			.architecture(architecture)
			.version(version)
			.build();
	}

	private static HtmlParserResultTester assertThat(Platform platform, String versionList) {
		return HtmlParserResultTester.with(
			new AmazonPackageResolver(Command.Mongo),
			version -> Distribution.of(Version.of(version), platform),
			versionList);
	}

}