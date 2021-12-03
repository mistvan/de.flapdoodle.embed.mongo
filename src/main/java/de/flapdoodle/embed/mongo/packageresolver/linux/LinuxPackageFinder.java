/**
 * Copyright (C) 2011
 *   Michael Mosmann <michael@mosmann.de>
 *   Martin Jöhren <m.joehren@googlemail.com>
 *
 * with contributions from
 * 	konstantin-ba@github,Archimedes Trajano	(trajano@github)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.flapdoodle.embed.mongo.packageresolver.linux;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.packageresolver.*;
import de.flapdoodle.embed.process.config.store.*;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.os.BitSize;
import de.flapdoodle.os.OS;
import de.flapdoodle.os.linux.CentosVersion;
import de.flapdoodle.os.linux.UbuntuVersion;

import java.util.Optional;

public class LinuxPackageFinder implements PackageFinder {

  private final Command command;
  private final ImmutablePlatformMatchRules rules;

  public LinuxPackageFinder(Command command) {
    this.command = command;
    this.rules = rules(command);
  }

  @Override
  public Optional<DistributionPackage> packageFor(Distribution distribution) {
    return rules.packageFor(distribution);
  }

  private static ImmutablePlatformMatchRules rules(Command command) {
    ImmutableFileSet fileSet = FileSet.builder().addEntry(FileType.Executable, command.commandName()).build();

    ImmutablePlatformMatchRule ubuntuRule = PlatformMatchRule.builder()
            .match(PlatformMatch.withOs(OS.Linux)
                    .withVersion(UbuntuVersion.values()))
            .finder(new UbuntuPackageResolver(command))
            .build();

    ImmutablePlatformMatchRule centosRule = PlatformMatchRule.builder()
      .match(PlatformMatch.withOs(OS.Linux)
        .withVersion(CentosVersion.values()))
      .finder(new CentosPackageResolver(command))
      .build();

    /*
      Linux (legacy) undefined
      https://fastdl.mongodb.org/linux/mongodb-linux-i686-{}.tgz
      3.2.21 - 3.2.0, 3.0.14 - 3.0.0, 2.6.12 - 2.6.0
    */
    PlatformMatchRule legacy32 = PlatformMatchRule.builder()
            .match(DistributionMatch.any(
                            VersionRange.of("3.2.0", "3.2.21"),
                            VersionRange.of("3.0.0", "3.0.14"),
                            VersionRange.of("2.6.0", "2.6.12")
                    )
                    .andThen(PlatformMatch.withOs(OS.Linux).withBitSize(BitSize.B32)))
            .finder(UrlTemplatePackageResolver.builder()
                    .fileSet(fileSet)
                    .archiveType(ArchiveType.TGZ)
                    .urlTemplate("/linux/mongodb-linux-i686-{version}.tgz")
                    .build())
            .build();

  /*
    Linux (legacy) x64
    https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-{}.tgz
    4.0.26 - 4.0.0, 3.6.22 - 3.6.0, 3.4.23 - 3.4.9, 3.4.7 - 3.4.0, 3.2.21 - 3.2.0, 3.0.14 - 3.0.0, 2.6.12 - 2.6.0
   */
    PlatformMatchRule legacy64 = PlatformMatchRule.builder()
            .match(DistributionMatch.any(
                            VersionRange.of("4.0.0", "4.0.26"),
                            VersionRange.of("3.6.0", "3.6.22"),
                            VersionRange.of("3.4.9", "3.4.23"),
                            VersionRange.of("3.4.0", "3.4.7"),
                            VersionRange.of("3.2.0", "3.2.21"),
                            VersionRange.of("3.0.0", "3.0.14"),
                            VersionRange.of("2.6.0", "2.6.12")
                    )
                    .andThen(PlatformMatch.withOs(OS.Linux).withBitSize(BitSize.B64)))
            .finder(UrlTemplatePackageResolver.builder()
                    .fileSet(fileSet)
                    .archiveType(ArchiveType.TGZ)
                    .urlTemplate("/linux/mongodb-linux-x86_64-{version}.tgz")
                    .build())
            .build();

    PlatformMatchRule hiddenLegacy64 = PlatformMatchRule.builder()
            .match(DistributionMatch.any(
                            VersionRange.of("3.3.1", "3.3.1"),
                            VersionRange.of("3.5.5", "3.5.5")
                    )
                    .andThen(PlatformMatch.withOs(OS.Linux).withBitSize(BitSize.B64)))
            .finder(UrlTemplatePackageResolver.builder()
                    .fileSet(fileSet)
                    .archiveType(ArchiveType.TGZ)
                    .urlTemplate("/linux/mongodb-linux-x86_64-{version}.tgz")
                    .build())
            .build();

    PlatformMatchRule hiddenLegacy32 = PlatformMatchRule.builder()
            .match(DistributionMatch.any(
                            VersionRange.of("3.3.1", "3.3.1"),
                            VersionRange.of("3.5.5", "3.5.5")
                    )
                    .andThen(PlatformMatch.withOs(OS.Linux).withBitSize(BitSize.B32)))
            .finder(UrlTemplatePackageResolver.builder()
                    .fileSet(fileSet)
                    .archiveType(ArchiveType.TGZ)
                    .urlTemplate("/linux/mongodb-linux-i686-{version}.tgz")
                    .build())
            .build();


    PlatformMatchRule failIfNothingMatches = PlatformMatchRule.builder()
            .match(PlatformMatch.withOs(OS.Linux))
            .finder(distribution -> {
              throw new IllegalArgumentException("linux distribution not supported: " + distribution);
            })
            .build();

    return PlatformMatchRules.empty()
            .withRules(
                    ubuntuRule,
                    legacy32,
                    legacy64,
                    hiddenLegacy64,
                    hiddenLegacy32,
                    failIfNothingMatches
            );
  }

}
