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
import de.flapdoodle.embed.mongo.packageresolver.linux.LinuxPackageResolver;
import de.flapdoodle.embed.process.config.store.DistributionPackage;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.config.store.PackageResolver;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.os.OS;

import java.util.Optional;

/**
 * bc mongodb decided to reinvent their artifact naming which is some kind of complex
 * we have to deal with that somehow
 */
public class CrazyNamingMongoDBPackageResolver implements PackageResolver {

  private final Command command;
  private ImmutablePlatformMatchRules rules=PlatformMatchRules.empty();

  public CrazyNamingMongoDBPackageResolver(Command command) {
    this.command = command;


    forPlatform(PlatformMatch.withOs(OS.Windows))
            .resolveWith(new WindowsPackageResolver(command));
    forPlatform(PlatformMatch.withOs(OS.OS_X))
            .resolveWith(new OSXPackageResolver(command));
    forPlatform(PlatformMatch.withOs(OS.Linux))
            .resolveWith(new LinuxPackageResolver(command));
    forPlatform(PlatformMatch.withOs(OS.Solaris))
            .resolveWith(new SolarisPackageFinder(command));

    forPlatform(PlatformMatch.any())
            .resolveWith(distribution -> {
              throw new IllegalArgumentException("could not resolve package for " + distribution);
            });
//    forPlatform(PlatformMatch.any())
//            .resolveWith("https://fastdl.mongodb.org/linux/mongodb-linux-aarch64-ubuntu1804-4.4.5.tgz");
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
      PackageFinder packageResolver = distribution -> Optional.of(DistributionPackage.of(archiveType,
              getFileSet(distribution.platform().operatingSystem()),
              url
      ));

      resolveWith(packageResolver);
    }
    
    private void resolveWith(PackageFinder finder) {
      rules = rules.with(PlatformMatchRule.of(match, finder));
    }
  }

  @Override
  public DistributionPackage packageFor(Distribution distribution) {
    Optional<DistributionPackage> result = rules.packageFor(distribution);
    return result.orElseThrow(() -> new IllegalArgumentException("could not resolve package for "+distribution));
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
}
