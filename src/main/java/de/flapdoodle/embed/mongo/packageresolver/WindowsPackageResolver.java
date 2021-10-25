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

  static {

  }

}
