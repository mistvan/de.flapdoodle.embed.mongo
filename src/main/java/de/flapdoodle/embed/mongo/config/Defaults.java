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
package de.flapdoodle.embed.mongo.config;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.Paths;
import de.flapdoodle.embed.process.config.ImmutableRuntimeConfig;
import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.config.store.DistributionDownloadPath;
import de.flapdoodle.embed.process.config.store.DownloadConfig;
import de.flapdoodle.embed.process.config.store.ImmutableDownloadConfig;
import de.flapdoodle.embed.process.config.store.PackageResolver;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Platform;
import de.flapdoodle.embed.process.extract.DirectoryAndExecutableNaming;
import de.flapdoodle.embed.process.extract.NoopTempNaming;
import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import de.flapdoodle.embed.process.io.directories.Directory;
import de.flapdoodle.embed.process.io.directories.FixedPath;
import de.flapdoodle.embed.process.io.directories.PropertyOrPlatformTempDir;
import de.flapdoodle.embed.process.io.directories.UserHome;
import de.flapdoodle.embed.process.io.progress.Slf4jProgressListener;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;
import de.flapdoodle.embed.process.runtime.CommandLinePostProcessor;
import de.flapdoodle.embed.process.store.ExtractedArtifactStore;
import de.flapdoodle.embed.process.store.ImmutableExtractedArtifactStore;
import de.flapdoodle.embed.process.store.UrlConnectionDownloader;

public abstract class Defaults {

	public static ImmutableExtractedArtifactStore extractedArtifactStoreFor(Command command) {
		return ExtractedArtifactStore.builder()
				.downloadConfig(Defaults.downloadConfigFor(command).build())
				.downloader(new UrlConnectionDownloader())
				.extraction(DirectoryAndExecutableNaming.builder()
						.directory(new UserHome(".embedmongo/extracted"))
						.executableNaming(new NoopTempNaming())
						.build())
				.temp(DirectoryAndExecutableNaming.builder()
						.directory(new PropertyOrPlatformTempDir())
						.executableNaming(new UUIDTempNaming())
						.build())
				.build();
	}
	
	public static ImmutableDownloadConfig.Builder downloadConfigFor(Command command) {
		return DownloadConfigDefaults.defaultsForCommand(command);
	}
	
	public static ImmutableDownloadConfig.Builder downloadConfigDefaults() {
		return DownloadConfigDefaults.withDefaults();
	}
	
	protected static class DownloadConfigDefaults {
		protected static ImmutableDownloadConfig.Builder defaultsForCommand(Command command) {
			return withDefaults().packageResolver(packageResolver(command));
		}
		
		protected static ImmutableDownloadConfig.Builder withDefaults() {
			return DownloadConfig.builder()
					.fileNaming(new UUIDTempNaming())
					.downloadPath(new PlatformDependentDownloadPath())
					.progressListener(new StandardConsoleProgressListener())
					.artifactStorePath(defaultArtifactStoreLocation())
					.downloadPrefix("embedmongo-download")
					.userAgent("Mozilla/5.0 (compatible; Embedded MongoDB; +https://github.com/flapdoodle-oss/embedmongo.flapdoodle.de)");
		}
		
		public static PackageResolver packageResolver(Command command) {
			return new Paths(command);
		}

		private static Directory defaultArtifactStoreLocation() {
			return defaultArtifactStoreLocation(System.getenv());
		}

		protected static Directory defaultArtifactStoreLocation(Map<String, String> env) {
			Optional<String> artifactStoreLocationEnvironmentVariable = Optional.ofNullable(env.get("EMBEDDED_MONGO_ARTIFACTS"));
			if (artifactStoreLocationEnvironmentVariable.isPresent()) {
				return new FixedPath(artifactStoreLocationEnvironmentVariable.get());
			}
			else {
				return new UserHome(".embedmongo");
			}
		}

		private static class PlatformDependentDownloadPath implements DistributionDownloadPath {

			@Override
			public String getPath(Distribution distribution) {
				if (distribution.platform()==Platform.Windows) {
					return "https://downloads.mongodb.org/";
				}
				return "https://fastdl.mongodb.org/";
			}
			
		}

	}
	
	public static ImmutableRuntimeConfig.Builder runtimeConfigFor(Command command, Logger logger) {
		return RuntimeConfigDefaults.defaultsWithLogger(command, logger);
	}
	
	public static ImmutableRuntimeConfig.Builder runtimeConfigFor(Command command) {
		return RuntimeConfigDefaults.defaults(command);
	}
	
	protected static class RuntimeConfigDefaults {

		protected static ImmutableRuntimeConfig.Builder defaultsWithLogger(Command command, Logger logger) {
			DownloadConfig downloadConfig = Defaults.downloadConfigFor(command)
					.progressListener(new Slf4jProgressListener(logger))
					.build();
			return defaults(command)
				.processOutput(MongodProcessOutputConfig.getInstance(command, logger))
				.artifactStore(Defaults.extractedArtifactStoreFor(command).withDownloadConfig(downloadConfig));
		}
		
		protected static ImmutableRuntimeConfig.Builder defaults(Command command) {
			return RuntimeConfig.builder()
			.processOutput(MongodProcessOutputConfig.getDefaultInstance(command))
			.commandLinePostProcessor(new CommandLinePostProcessor.Noop())
			.artifactStore(Defaults.extractedArtifactStoreFor(command));
		}
	}
}
