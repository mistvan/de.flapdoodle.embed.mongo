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

import java.nio.file.Files;
import java.util.*;
import java.util.function.Supplier;

import de.flapdoodle.embed.mongo.commands.CommandArguments;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.packageresolver.PlatformPackageResolver;
import de.flapdoodle.embed.mongo.transitions.CommandProcessArguments;
import de.flapdoodle.embed.mongo.transitions.MongodProcessArguments;
import de.flapdoodle.embed.mongo.transitions.MongodStarter;
import de.flapdoodle.embed.mongo.types.DatabaseDir;
import de.flapdoodle.embed.process.archives.ArchiveType;
import de.flapdoodle.embed.process.config.SupportConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.config.store.*;
import de.flapdoodle.embed.process.config.store.Package;
import de.flapdoodle.embed.process.nio.Directories;
import de.flapdoodle.embed.process.nio.directories.PersistentDir;
import de.flapdoodle.embed.process.nio.directories.TempDir;
import de.flapdoodle.embed.process.store.*;
import de.flapdoodle.embed.process.transitions.*;
import de.flapdoodle.embed.process.types.Name;
import de.flapdoodle.embed.process.types.ProcessArguments;
import de.flapdoodle.embed.process.types.ProcessConfig;
import de.flapdoodle.embed.process.types.ProcessEnv;
import de.flapdoodle.os.Platform;
import de.flapdoodle.reverse.State;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.reverse.edges.Derive;
import de.flapdoodle.reverse.edges.Join;
import de.flapdoodle.reverse.edges.Start;
import de.flapdoodle.types.Try;
import org.slf4j.Logger;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.process.config.ImmutableRuntimeConfig;
import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
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

public abstract class Defaults {

	public static <C extends CommandArguments, T extends CommandProcessArguments<C>> List<Transition<?>> transitionsFor(
		T processArguments,
		C arguments, Version.Main version
	) {

		PersistentDir baseDir = PersistentDir.userHome(".embedmongo").get();
		Command command=arguments.command();

		// TODO use same legacy directory?
		ArchiveStore archiveStore = new LocalArchiveStore(baseDir.value().resolve("archives"));
		ExtractedFileSetStore extractedFileSetStore = new ContentHashExtractedFileSetStore(baseDir.value().resolve("fileSets"));
		PlatformPackageResolver legacyPackageResolver = new PlatformPackageResolver(command);

		return Arrays.asList(
			InitTempDirectory.withPlatformTemp(),

			Start.to(Name.class).initializedWith(Name.of(command.commandName())).withTransitionLabel("create Name"),

			Start.to(SupportConfig.class)
				.initializedWith(SupportConfig.builder()
					.name(command.commandName())
					.messageOnException((clazz,ex) -> null)
					.supportUrl("https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo/issues")
					.build()).withTransitionLabel("create default"),

			Start.to(ProcessConfig.class).initializedWith(ProcessConfig.defaults()).withTransitionLabel("create default"),
			Start.to(ProcessEnv.class).initializedWith(ProcessEnv.of(Collections.emptyMap())).withTransitionLabel("create empty env"),

			Start.to(de.flapdoodle.embed.process.distribution.Version.class).initializedWith(version),
			
			Start.to(processArguments.arguments()).initializedWith(arguments),
			Start.to(Net.class).providedBy(Net::defaults),

			Derive.given(Name.class).state(ProcessOutput.class)
				.deriveBy(name -> ProcessOutput.namedConsole(name.value()))
				.withTransitionLabel("create named console"),

			Derive.given(TempDir.class).state(DatabaseDir.class)
				.with(tempDir -> {
					DatabaseDir databaseDir = Try.get(() -> DatabaseDir.of(tempDir.createDirectory("mongod-database")));
					return State.of(databaseDir, dir -> Try.run(() -> Directories.deleteAll(dir.value())));
				}),

			processArguments,

			Start.to(Platform.class).providedBy(Platform::detect),

			Join.given(de.flapdoodle.embed.process.distribution.Version.class).and(Platform.class).state(Distribution.class)
				.deriveBy(Distribution::of)
				.withTransitionLabel("version + platform"),

			PackageOfDistribution.with(distribution -> {
				DistributionPackage legacyPackage = legacyPackageResolver.packageFor(distribution);
				return Package.of(archiveTypeOfLegacy(legacyPackage.archiveType()),legacyPackage.fileSet(), "https://fastdl.mongodb.org"+legacyPackage.archivePath());
			}),

			DownloadPackage.with(archiveStore),

			ExtractPackage.withDefaults()
				.withExtractedFileSetStore(extractedFileSetStore),


			MongodStarter.withDefaults()
		);
	}
	private static ArchiveType archiveTypeOfLegacy(de.flapdoodle.embed.process.distribution.ArchiveType archiveType) {
		switch (archiveType) {
			case EXE:
				return ArchiveType.EXE;
			case TBZ2:
				return ArchiveType.TBZ2;
			case TGZ:
				return ArchiveType.TGZ;
			case ZIP:
				return ArchiveType.ZIP;
			case TXZ:
				return ArchiveType.TXZ;
		}
		throw new IllegalArgumentException("Could not map: "+archiveType);
	}

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
					.downloadPath(new StaticDownloadPath())
					.progressListener(new StandardConsoleProgressListener())
					.artifactStorePath(defaultArtifactStoreLocation())
					//.downloadPrefix("embedmongo-download")
					.userAgent("Mozilla/5.0 (compatible; Embedded MongoDB; +https://github.com/flapdoodle-oss/embedmongo.flapdoodle.de)");
		}
		
		public static PackageResolver packageResolver(Command command) {
			return new PlatformPackageResolver(command);
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

		private static class StaticDownloadPath implements DistributionDownloadPath {

			@Override
			public String getPath(Distribution distribution) {
				return "https://fastdl.mongodb.org";
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
