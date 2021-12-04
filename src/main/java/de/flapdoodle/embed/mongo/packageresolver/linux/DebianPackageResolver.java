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
import de.flapdoodle.embed.process.config.store.DistributionPackage;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.config.store.ImmutableFileSet;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.os.BitSize;
import de.flapdoodle.os.CPUType;
import de.flapdoodle.os.OS;
import de.flapdoodle.os.linux.DebianVersion;

import java.util.Optional;

public class DebianPackageResolver implements PackageFinder {

    private final ImmutablePlatformMatchRules rules;

    public DebianPackageResolver(final Command command) {
        this.rules = rules(command);
    }

    @Override
    public Optional<DistributionPackage> packageFor(final Distribution distribution) {
        return rules.packageFor(distribution);
    }

    private static ImmutablePlatformMatchRules rules(final Command command) {
        final ImmutableFileSet fileSet = FileSet.builder().addEntry(FileType.Executable, command.commandName()).build();

    /*
      Debian 9 x64
      https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-debian92-{}.tgz
      3.6.23 - 5.0.4
     */
        final PlatformMatchRule debian9 = PlatformMatchRule.builder()
                .match(DistributionMatch.any(VersionRange.of("3.6.23", "5.0.4"))
                        .andThen(PlatformMatch
                                .withOs(OS.Linux)
                                .withBitSize(BitSize.B64)
                                .withCpuType(CPUType.X86)
                                .withVersion(DebianVersion.DEBIAN_9)
                        )
                )
                .finder(UrlTemplatePackageResolver.builder()
                        .fileSet(fileSet)
                        .archiveType(ArchiveType.TGZ)
                        .urlTemplate("/linux/mongodb-linux-x86_64-debian92-{version}.tgz")
                        .build())
                .build();

        final PlatformMatchRule debian9tools = PlatformMatchRule.builder()
                .match(DistributionMatch.any(VersionRange.of("3.6.23", "5.0.4"))
                        .andThen(PlatformMatch
                                .withOs(OS.Linux)
                                .withBitSize(BitSize.B64)
                                .withCpuType(CPUType.X86)
                                .withVersion(DebianVersion.DEBIAN_9)
                        )
                )
                .finder(UrlTemplatePackageResolver.builder()
                        .fileSet(fileSet)
                        .archiveType(ArchiveType.TGZ)
                        .urlTemplate("/tools/db/mongodb-database-tools-debian92-x86_64-{tools.version}.tgz")
                        .build())
                .build();

    /*
      Debian 10 x64
      https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-debian10-{}.tgz
      4.2.13 - 5.0.4
     */
        final PlatformMatchRule debian10 = PlatformMatchRule.builder()
                .match(DistributionMatch.any(VersionRange.of("4.2.13", "5.0.4"))
                        .andThen(PlatformMatch
                                .withOs(OS.Linux)
                                .withBitSize(BitSize.B64)
                                .withCpuType(CPUType.X86)
                                .withVersion(DebianVersion.DEBIAN_10)
                        )
                )
                .finder(UrlTemplatePackageResolver.builder()
                        .fileSet(fileSet)
                        .archiveType(ArchiveType.TGZ)
                        .urlTemplate("/linux/mongodb-linux-x86_64-debian10-{version}.tgz")
                        .build())
                .build();

        final PlatformMatchRule debian10tools = PlatformMatchRule.builder()
                .match(DistributionMatch.any(VersionRange.of("4.2.13", "5.0.4"))
                        .andThen(PlatformMatch
                                .withOs(OS.Linux)
                                .withBitSize(BitSize.B64)
                                .withCpuType(CPUType.X86)
                                .withVersion(DebianVersion.DEBIAN_10)
                        )
                )
                .finder(UrlTemplatePackageResolver.builder()
                        .fileSet(fileSet)
                        .archiveType(ArchiveType.TGZ)
                        .urlTemplate("/tools/db/mongodb-database-tools-debian10-x86_64-{tools.version}.tgz")
                        .build())
                .build();

        switch (command) {
            case MongoDump:
            case MongoImport:
            case MongoRestore:
                return PlatformMatchRules.empty()
                        .withRules(
                                debian9tools,
                                debian10tools
                        );
            default:
                return PlatformMatchRules.empty()
                        .withRules(
                                debian9,
                                debian10
                        );
        }
    }
}
