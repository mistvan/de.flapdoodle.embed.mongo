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
import de.flapdoodle.os.CPUType;
import de.flapdoodle.os.OS;
import de.flapdoodle.os.Platform;

import java.util.Optional;

public class WindowsPackageResolver implements PackageResolver {
  private final OS os;
  private final Command command;

  public WindowsPackageResolver(OS os, Command command) {
    this.os = os;
    this.command = command;
  }

  @Override
  public DistributionPackage packageFor(Distribution distribution) {
    Platform platform = distribution.platform();
    
    if (platform.operatingSystem()!=os) throw new IllegalArgumentException("os does not match: "+ platform.operatingSystem());
    if (platform.architecture().cpuType()== CPUType.ARM) throw new IllegalArgumentException("cpu type not supported: "+ platform.architecture().cpuType());

    return pathOf(distribution)
            .map(path -> DistributionPackage.of(ArchiveType.ZIP, fileSetOf(command), path))
            .orElse(null);
  }

  private static Optional<String> pathOf(Distribution distribution) {
    if (distribution.version() instanceof IFeatureAwareVersion) {
      IFeatureAwareVersion version = (IFeatureAwareVersion) distribution.version();
      if (version.equals(Version.V4_2_13)) {
        return Optional.of("https://fastdl.mongodb.org/win32/mongodb-win32-x86_64-2012plus-4.2.13.zip");
      }
    }
    return Optional.empty();
  }

  private static FileSet fileSetOf(Command command) {
    return FileSet.builder()
            .addEntry(FileType.Executable, command.commandName() + ".exe")
            .build();
  }

}
