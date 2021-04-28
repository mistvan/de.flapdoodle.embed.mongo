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
package de.flapdoodle.embed.mongo;

import de.flapdoodle.embed.mongo.distribution.Feature;
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
import de.flapdoodle.embed.process.config.store.DistributionPackage;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.config.store.PackageResolver;
import de.flapdoodle.embed.process.distribution.Architecture;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.BitSize;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Platform;
import de.flapdoodle.embed.process.distribution.Version;

/**
 *
 */
public class Paths implements PackageResolver {

    /**
     * A marker indicating that there is no user defined Linux distribution.
     * In this case the newest Ubuntu version will be selected.
     */
    private static final String UNKNOWN_LINUX_DISTRO = "unknown-linux-distro";
	/**
	 * A system property that could be used to set the preferred Linux distribution
	 * See <a href="https://www.mongodb.com/download-center/community/releases">Mongodb download</a> for their list.
	 */
	// Later this could be made smarter by using lsb_release or similar to auto-detect the distro and its version
	private static final String LINUX_DISTRO = System.getProperty("de.flapdoodle.embed.mongo.linux.distro", UNKNOWN_LINUX_DISTRO);

	private final Command command;

	public Paths(Command command) {
		this.command=command;
	}
	
	@Override
	public DistributionPackage packageFor(Distribution distribution) {
		return DistributionPackage.of(getArchiveType(distribution), getFileSet(distribution), getPath(distribution));
	}

	public FileSet getFileSet(Distribution distribution) {
		String executableFileName;
		switch (distribution.platform()) {
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
				throw new IllegalArgumentException("Unknown Platform " + distribution.platform());
		}
		return FileSet.builder().addEntry(FileType.Executable, executableFileName).build();
	}

	//CHECKSTYLE:OFF
	public ArchiveType getArchiveType(Distribution distribution) {
		ArchiveType archiveType;
		switch (distribution.platform()) {
			case Linux:
			case OS_X:
			case Solaris:
			case FreeBSD:
				archiveType = ArchiveType.TGZ;
				break;
			case Windows:
				archiveType = ArchiveType.ZIP;
				break;
			default:
				throw new IllegalArgumentException("Unknown Platform " + distribution.platform());
		}
		return archiveType;
	}

	public String getPath(Distribution distribution) {

		if (distribution.platform() == Platform.Solaris && isFeatureEnabled(distribution, Feature.NO_SOLARIS_SUPPORT)) {
			throw new IllegalArgumentException("Mongodb for Solaris is not available anymore");
		}

		ArchiveType archiveType = getArchiveType(distribution);
		String archiveTypeStr = getArchiveString(archiveType);

		String platformStr = getPlatformString(distribution);

		String bitSizeStr = getBitSize(distribution);
		String bitSizeAndOsStr = enrichBitSizeWithOsDetails(distribution, bitSizeStr);
		String versionStr = getVersionPart(distribution.version());

		return platformStr + "/mongodb-" + platformStr + "-" + bitSizeAndOsStr + "-" + versionStr + "." + archiveTypeStr;
	}

    protected String enrichBitSizeWithOsDetails(Distribution distribution, String bitSizeStr) {
	    String result = bitSizeStr;
        if (distribution.platform()==Platform.Windows && distribution.bitsize()==BitSize.B64) {
            result += getWindowsDetails(distribution);
        } else if (distribution.platform() == Platform.Linux) {
            result += getLinuxDetails(distribution);
        } else if (distribution.platform() == Platform.OS_X && withSsl(distribution)) {
            result = (withSsl(distribution) ? "ssl-" : "") + result;
        }
	    return result;
    }

    protected String getWindowsDetails(Distribution distribution) {
        String result = "";
        if (useWindows2008PlusVersion(distribution)) {
            result += "-2008plus";
        }

        if (useWindows2012PlusVersion(distribution)) {
            result += "-2012plus";
        }

        if (withSsl(distribution)) {
            result += "-ssl";
        }
        return result;
    }

    protected String getArchiveString(ArchiveType archiveType) {
        String sarchiveType;
        switch (archiveType) {
            case TGZ:
                sarchiveType = "tgz";
                break;
            case ZIP:
                sarchiveType = "zip";
                break;
            default:
                throw new IllegalArgumentException("Unknown ArchiveType " + archiveType);
        }
        return sarchiveType;
    }

    protected String getPlatformString(Distribution distribution) {
        String splatform;
        switch (distribution.platform()) {
            case Linux:
                splatform = "linux";
                break;
            case Windows:
                if (distribution.version().isNewerOrEqual(4, 4, 0)) {
                    splatform = "windows";
                } else {
                    splatform = "win32";
                }
                break;
            case OS_X:
                splatform = "osx";
                break;
            case Solaris:
                splatform = "sunos5";
                break;
            case FreeBSD:
                splatform = "freebsd";
                break;
            default:
                throw new IllegalArgumentException("Unknown Platform " + distribution.platform());
        }
        return splatform;
    }

    protected String getBitSize(Distribution distribution) {
        String sbitSize;
        final Version version = distribution.version();
        switch (distribution.bitsize()) {
            case B32:
                if (version instanceof IFeatureAwareVersion) {
                    IFeatureAwareVersion featuredVersion = (IFeatureAwareVersion) version;
                    if (featuredVersion.enabled(Feature.ONLY_64BIT)) {
                        throw new IllegalArgumentException("this version does not support 32Bit: "+distribution);
                    }
                }

                switch (distribution.platform()) {
                    case Linux:
                        sbitSize = "i686";
                        break;
                    case Windows:
                    case OS_X:
                        sbitSize = "i386";
                        break;
                    default:
                        throw new IllegalArgumentException("Platform " + distribution.platform() + " not supported yet on 32Bit Platform");
                }
                break;
            case B64:
                if (distribution.architecture() == Architecture.AARCH64) {
                    if (version.isOlderOrEqual(4, 1, Integer.MAX_VALUE)) {
                        sbitSize = "arm64";
                    } else {
                        sbitSize = "aarch64";
                    }
                } else {
                    sbitSize = "x86_64";
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown BitSize " + distribution.bitsize());
        }
        return sbitSize;
    }

    protected boolean useWindows2008PlusVersion(Distribution distribution) {
        final Version version = distribution.version();
        return distribution.platform() == Platform.Windows
                && version instanceof IFeatureAwareVersion
                && ((IFeatureAwareVersion) version).enabled(Feature.ONLY_WINDOWS_2008_SERVER);
    }

    protected boolean useWindows2012PlusVersion(Distribution distribution) {
        final Version version = distribution.version();
        return distribution.platform() == Platform.Windows
                && version instanceof IFeatureAwareVersion
                && ((IFeatureAwareVersion) version).enabled(Feature.ONLY_WINDOWS_2012_SERVER);
    }

    protected boolean withSsl(Distribution distribution) {
        if ((distribution.platform() == Platform.Windows || distribution.platform() == Platform.OS_X)
                && distribution.version() instanceof IFeatureAwareVersion) {
            return ((IFeatureAwareVersion) distribution.version()).enabled(Feature.ONLY_WITH_SSL);
        } else {
            return false;
        }
    }

    private static boolean isFeatureEnabled(Distribution distribution, Feature feature) {
	    return (distribution.version() instanceof IFeatureAwareVersion
                &&  ((IFeatureAwareVersion) distribution.version()).enabled(feature));
    }

    protected String getLinuxDetails(Distribution distribution) {
        String result;
        final Version version = distribution.version();
        final String distro = getLinuxDistro(version);
        if (distribution.architecture() == Architecture.AARCH64) {
            if (version.isNewerOrEqual(4, 2, 0)) {
                result = "-aarch64-" + distro;
            } else if (version.isNewerOrEqual(3, 4, 0)) {
                result = "-arm64-ubuntu1604";
            } else {
                throw new IllegalArgumentException("Mongodb does not support ARM64 in version " + version);
            }
        } else if (version.isNewerOrEqual(3, 6, 0)) {
            result = "-" + distro;
        } else {
            result = "";
        }

        return result;
    }

    protected String getLinuxDistro(Version version) {
        String result = "";
	    if (!UNKNOWN_LINUX_DISTRO.equals(LINUX_DISTRO)) {
            // use the user defined linux distro
            result = LINUX_DISTRO;
        } else if (version.isNewerOrEqual(4, 4, 0)) {
            result = "ubuntu2004";
	    } else if (version.isNewerOrEqual(3, 6, 20)) {
            result = "ubuntu1804";
        } else if (version.isNewerOrEqual(3, 2, 7)) {
            result = "ubuntu1604";
        } else if (version.isNewerOrEqual(3, 0, 0)) {
            result = "ubuntu1404";
        }
        return result;
    }

    protected static String getVersionPart(Version version) {
        return version.asInDownloadPath();
    }

}
