package de.flapdoodle.embed.mongo.packageresolver;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.store.DistributionPackage;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.config.store.PackageResolver;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.os.*;
import de.flapdoodle.os.linux.LinuxDistribution;
import de.flapdoodle.os.linux.UbuntuVersion;

import java.util.Optional;

/**
 * bc mongodb decided to reinvent their artifact naming which is some kind of complex
 * we have to deal with that somehow
 */
public class CrazyNamingMongoDBPackageResolver implements PackageResolver {

  private final Command command;
  private final PackageResolver fallback;
  private ImmutablePlatformMatchRules rules=PlatformMatchRules.empty();

  public CrazyNamingMongoDBPackageResolver(Command command, PackageResolver fallback) {
    this.command = command;
    this.fallback = fallback;

    forPlatform(PlatformMatch.withOs(OS.Windows))
            .resolveWith(new WindowsPackageResolver(OS.Windows, command));
    
    forPlatform(PlatformMatch.any())
            .resolveWith("https://fastdl.mongodb.org/linux/mongodb-linux-aarch64-ubuntu1804-4.4.5.tgz");
  }

  private WithMatch forPlatform(PlatformMatch match) {
    return new WithMatch(match);
  }

  private class WithMatch {

    private final PlatformMatch match;

    public WithMatch(PlatformMatch match) {
      this.match = match;
    }

    private void resolveWith(String url) {
      resolveWith(ArchiveType.TGZ, url);
    }

    private void resolveWith(ArchiveType archiveType, String url) {
      PackageResolver packageResolver = distribution -> DistributionPackage.of(archiveType,
              getFileSet(distribution.platform().operatingSystem()),
              url
      );

      resolveWith(packageResolver);
    }
    
    private void resolveWith(PackageResolver resolver) {
      rules = rules.with(PlatformMatchRule.of(match, resolver));
    }
  }

  @Override
  public DistributionPackage packageFor(Distribution distribution) {
    for (PlatformMatchRule rule : rules.rules()) {
      if (PlatformMatch.match(rule.match(), distribution)) {
        return rule.resolver().packageFor(distribution);
      }
    }
    return fallback.packageFor(distribution);
  }

  private FileSet getFileSet(OS os) {
    String executableFileName;
    switch (os) {
      case Linux:
      case OS_X:
      case Solaris:
      case FreeBSD:
        executableFileName = command.commandName();
        break;
      case Windows:
        executableFileName = command.commandName()+".exe";
        break;
      default:
        throw new IllegalArgumentException("Unknown OS " + os);
    }
    return FileSet.builder().addEntry(FileType.Executable, executableFileName).build();
  }

  private static Optional<de.flapdoodle.embed.mongo.distribution.Version> predefinedVersionOf(de.flapdoodle.embed.process.distribution.Version version) {
    if (version instanceof Version) {
      return Optional.of((Version) version);
    }
    if (version instanceof Version.Main) {
      return predefinedVersionOf(((Version.Main) version).latest());
    }
    return Optional.empty();
  }

}
