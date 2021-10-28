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
package de.flapdoodle.embed.mongo.packageresolver;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.Paths;
import de.flapdoodle.embed.process.config.store.DistributionPackage;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.os.BitSize;
import de.flapdoodle.os.OS;

import java.util.Optional;


public class OSXPackageResolver implements PackageFinder {
  private final Command command;
  private final ImmutablePlatformMatchRules rules;

  public OSXPackageResolver(Command command) {
    this.command = command;
    this.rules = rules(command);
  }

  @Override
  public Optional<DistributionPackage> packageFor(Distribution distribution) {
    return rules.packageFor(distribution);
  }

  private static FileSet fileSetOf(Command command) {
    return FileSet.builder()
            .addEntry(FileType.Executable, command.commandName())
            .build();
  }

  private static ImmutablePlatformMatchRules rules(Command command) {
    FileSet fileSet = fileSetOf(command);
    ArchiveType archiveType = ArchiveType.TGZ;

    /*
      https://fastdl.mongodb.org/osx/mongodb-osx-ssl-x86_64-{}.tgz
      4.0.26 - 4.0.0, 3.6.22 - 3.6.0
     */
    ImmutablePlatformMatchRule firstRule = PlatformMatchRule.builder()
            .match(DistributionMatch.any(
                            VersionRange.of("4.0.0", "4.0.26"),
                            VersionRange.of("3.6.0", "3.6.22")
                    )
                    .andThen(PlatformMatch.withOs(OS.OS_X).withBitSize(BitSize.B64)))
            .finder(UrlTemplatePackageResolver.builder()
                    .fileSet(fileSet)
                    .archiveType(archiveType)
                    .urlTemplate("/osx/mongodb-osx-ssl-x86_64-{version}.tgz")
                    .build())
            .build();

    /*
      https://fastdl.mongodb.org/osx/mongodb-osx-ssl-x86_64-{}.tgz|https://fastdl.mongodb.org/osx/mongodb-osx-x86_64-{}.tgz
      3.4.23 - 3.4.9, 3.4.7 - 3.4.0, 3.2.21 - 3.2.0, 3.0.14 - 3.0.4
     */
    ImmutablePlatformMatchRule secondRule = PlatformMatchRule.builder()
            .match(DistributionMatch.any(
                            VersionRange.of("3.4.9", "3.4.23"),
                            VersionRange.of("3.4.0", "3.4.7"),
                            VersionRange.of("3.2.0", "3.2.21"),
                            VersionRange.of("3.0.4", "3.0.14")
                    )
                    .andThen(PlatformMatch.withOs(OS.OS_X).withBitSize(BitSize.B64)))
            .finder(UrlTemplatePackageResolver.builder()
                    .fileSet(fileSet)
                    .archiveType(archiveType)
                    .urlTemplate("/osx/mongodb-osx-ssl-x86_64-{version}.tgz")
                    .build())
            .build();

    /*
      https://fastdl.mongodb.org/osx/mongodb-osx-x86_64-{}.tgz
      3.0.3 - 3.0.0, 2.6.12 - 2.6.0
     */
    ImmutablePlatformMatchRule thirdRule = PlatformMatchRule.builder()
            .match(DistributionMatch.any(
                            VersionRange.of("3.0.0", "3.0.3"),
                            VersionRange.of("2.6.0", "2.6.12")
                    )
                    .andThen(PlatformMatch.withOs(OS.OS_X).withBitSize(BitSize.B64)))
            .finder(UrlTemplatePackageResolver.builder()
                    .fileSet(fileSet)
                    .archiveType(archiveType)
                    .urlTemplate("/osx/mongodb-osx-x86_64-{version}.tgz")
                    .build())
            .build();

    ImmutablePlatformMatchRule hiddenLegacyRule = PlatformMatchRule.builder()
            .match(DistributionMatch.any(
                            VersionRange.of("3.3.1", "3.3.1"),
                            VersionRange.of("3.5.5", "3.5.5")
                    )
                    .andThen(PlatformMatch.withOs(OS.OS_X).withBitSize(BitSize.B64)))
            .finder(UrlTemplatePackageResolver.builder()
                    .fileSet(fileSet)
                    .archiveType(archiveType)
                    .urlTemplate("/osx/mongodb-osx-x86_64-{version}.tgz")
                    .build())
            .build();
    /*
      https://fastdl.mongodb.org/osx/mongodb-macos-x86_64-{}.tgz
      5.0.2 - 5.0.0, 4.4.9 - 4.4.0, 4.2.16 - 4.2.5, 4.2.3 - 4.2.0
    */
    ImmutablePlatformMatchRule fourthRule = PlatformMatchRule.builder()
            .match(DistributionMatch.any(
                            VersionRange.of("5.0.0", "5.0.2"),
                            VersionRange.of("4.4.0", "4.4.9"),
                            VersionRange.of("4.2.5", "4.2.16"),
                            VersionRange.of("4.2.0", "4.2.3")
                    )
                    .andThen(PlatformMatch.withOs(OS.OS_X).withBitSize(BitSize.B64)))
            .finder(UrlTemplatePackageResolver.builder()
                    .fileSet(fileSet)
                    .archiveType(archiveType)
                    .urlTemplate("/osx/mongodb-macos-x86_64-{version}.tgz")
                    .build())
            .build();

    PlatformMatchRule failIfNothingMatches = PlatformMatchRule.builder()
            .match(PlatformMatch.withOs(OS.OS_X))
            .finder(distribution -> {
              throw new IllegalArgumentException("osx distribution not supported: " + distribution);
            })
            .build();


    return PlatformMatchRules.empty()
            .withRules(
                    firstRule,
                    secondRule,
                    thirdRule,
                    fourthRule,
                    hiddenLegacyRule,
                    failIfNothingMatches
            );
  }

}
