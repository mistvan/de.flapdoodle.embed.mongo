package de.flapdoodle.embed.mongo.packageresolver;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.process.config.store.DistributionPackage;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.config.store.PackageResolver;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.os.BitSize;
import de.flapdoodle.os.OS;


public class OSXPackageResolver implements PackageResolver {
  private final OS os;
  private final Command command;
  private final ImmutablePlatformMatchRules rules;

  public OSXPackageResolver(OS os, Command command) {
    this.os = os;
    this.command = command;
    this.rules = rules(command);
  }

  @Override
  public DistributionPackage packageFor(Distribution distribution) {
    return rules.packageFor(distribution).orElse(null);
  }

  private static FileSet fileSetOf(Command command) {
    return FileSet.builder()
            .addEntry(FileType.Executable, command.commandName() + ".exe")
            .build();
  }

  /*
-----------------------------------
Windows Server 2008 R2+, without SSL x64
https://fastdl.mongodb.org/win32/mongodb-win32-x86_64-2008plus-{}.zip
3.4.23 - 3.4.9, 3.4.7 - 3.4.0, 3.2.21 - 3.2.0, 3.0.14 - 3.0.0, 2.6.12 - 2.6.0
--
5.0.2 - 5.0.0, 4.4.9 - 4.4.0, 4.2.16 - 4.2.0, 4.0.26 - 4.0.0, 3.6.22 - 3.6.0, 3.4.8
-----------------------------------
Windows x64
https://fastdl.mongodb.org/windows/mongodb-windows-x86_64-{}.zip
5.0.2 - 5.0.0, 4.4.9 - 4.4.0
https://fastdl.mongodb.org/win32/mongodb-win32-x86_64-2008plus-ssl-{}.zip
4.0.26 - 4.0.0, 3.6.22 - 3.6.0, 3.4.23 - 3.4.9, 3.4.7 - 3.4.0, 3.2.21 - 3.2.0, 3.0.14 - 3.0.0
https://fastdl.mongodb.org/win32/mongodb-win32-x86_64-2012plus-{}.zip
4.2.16 - 4.2.5, 4.2.3 - 4.2.0
--
4.2.4 - 4.2.4, 3.4.8 - 3.4.8, 2.6.12 - 2.6.0
-----------------------------------
windows_i686 undefined
https://fastdl.mongodb.org/win32/mongodb-win32-i386-{}.zip
3.2.21 - 3.2.0, 3.0.14 - 3.0.0, 2.6.12 - 2.6.0
--
5.0.2 - 5.0.0, 4.4.9 - 4.4.0, 4.2.16 - 4.2.0, 4.0.26 - 4.0.0, 3.6.22 - 3.6.0, 3.4.23 - 3.4.0
-----------------------------------
windows_x86_64 x64
https://fastdl.mongodb.org/win32/mongodb-win32-x86_64-{}.zip
3.4.23 - 3.4.9, 3.4.7 - 3.4.0, 3.2.21 - 3.2.0, 3.0.14 - 3.0.0, 2.6.12 - 2.6.0
--
5.0.2 - 5.0.0, 4.4.9 - 4.4.0, 4.2.16 - 4.2.0, 4.0.26 - 4.0.0, 3.6.22 - 3.6.0, 3.4.8

   */

  private static ImmutablePlatformMatchRules rules(Command command) {
    FileSet fileSet = fileSetOf(command);
    ArchiveType archiveType = ArchiveType.ZIP;

    ImmutablePlatformMatchRule win32rule = PlatformMatchRule.builder()
            .match(DistributionMatch.any(
                            VersionRange.of("3.2.0", "3.2.21"),
                            VersionRange.of("3.0.0", "3.0.14"),
                            VersionRange.of("2.6.0", "2.6.12")
                    )
                    .andThen(PlatformMatch.withOs(OS.Windows)
                            .withBitSize(BitSize.B32)))
            .resolver(UrlTemplatePackageResolver.builder()
                    .fileSet(fileSet)
                    .archiveType(archiveType)
                    .urlTemplate("https://fastdl.mongodb.org/win32/mongodb-win32-i386-{version}.zip")
                    .build())
            .build();

    ImmutablePlatformMatchRule win64rule2012 = PlatformMatchRule.builder()
            .match(DistributionMatch.any(
                    VersionRange.of("4.2.5", "4.2.16"),
                    VersionRange.of("4.2.0", "4.2.3")
            ).andThen(PlatformMatch.withOs(OS.Windows)
                    .withBitSize(BitSize.B64)))
            .resolver(UrlTemplatePackageResolver.builder()
                    .fileSet(fileSet)
                    .archiveType(archiveType)
                    .urlTemplate("https://fastdl.mongodb.org/win32/mongodb-win32-x86_64-2012plus-{version}.zip")
                    .build())
            .build();

    ImmutablePlatformMatchRule win64rule2008 = PlatformMatchRule.builder()
            .match(DistributionMatch.any(
                    VersionRange.of("4.0.0", "4.0.26"),
                    VersionRange.of("3.6.0", "3.6.22"),
                    VersionRange.of("3.4.9", "3.4.23"),
                    VersionRange.of("3.4.0", "3.4.7"),
                    VersionRange.of("3.2.0", "3.2.21"),
                    VersionRange.of("3.0.0", "3.0.14")
            ).andThen(PlatformMatch.withOs(OS.Windows)
                    .withBitSize(BitSize.B64)))
            .resolver(UrlTemplatePackageResolver.builder()
                    .fileSet(fileSet)
                    .archiveType(archiveType)
                    .urlTemplate("https://fastdl.mongodb.org/win32/mongodb-win32-x86_64-2008plus-ssl-{version}.zip")
                    .build())
            .build();

    ImmutablePlatformMatchRule failIfNothingMatches = PlatformMatchRule.builder()
            .match(PlatformMatch.withOs(OS.Windows))
            .resolver(distribution -> {
              throw new IllegalArgumentException("windows distribution not supported: "+distribution);
            })
            .build();

    return PlatformMatchRules.empty()
            .withRules(win32rule, win64rule2012, win64rule2008, failIfNothingMatches);
  }

}
