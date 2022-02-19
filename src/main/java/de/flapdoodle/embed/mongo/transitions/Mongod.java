package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.mongo.commands.MongodArguments;
import de.flapdoodle.embed.mongo.config.Defaults;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.packageresolver.Command;
import de.flapdoodle.embed.mongo.types.DatabaseDir;
import de.flapdoodle.embed.mongo.types.DistributionBaseUrl;
import de.flapdoodle.embed.process.archives.ExtractedFileSet;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.embed.process.nio.Directories;
import de.flapdoodle.embed.process.nio.directories.TempDir;
import de.flapdoodle.reverse.State;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.Transitions;
import de.flapdoodle.reverse.transitions.Derive;
import de.flapdoodle.reverse.transitions.Start;
import de.flapdoodle.types.Try;

public class Mongod implements WorkspaceDefaults, VersionAndPlatform, ProcessDefaults, CommandName, ExtractedFileSetFor {

	public Transitions transitions(de.flapdoodle.embed.process.distribution.Version version) {
		return workspaceDefaults()
			.addAll(versionAndPlatform())
			.addAll(processDefaults())
			.addAll(commandName())
			.addAll(extractedFileSetFor(StateID.of(ExtractedFileSet.class), StateID.of(Distribution.class), StateID.of(TempDir.class), StateID.of(Command.class), StateID.of(
				DistributionBaseUrl.class)))
			.addAll(
				Start.to(Command.class).initializedWith(Command.MongoD).withTransitionLabel("provide Command"),
				Start.to(de.flapdoodle.embed.process.distribution.Version.class).initializedWith(version),
				Start.to(Net.class).providedBy(Net::defaults),

				Derive.given(TempDir.class).state(DatabaseDir.class)
					.with(tempDir -> {
						DatabaseDir databaseDir = Try.get(() -> DatabaseDir.of(tempDir.createDirectory("mongod-database")));
						return State.of(databaseDir, dir -> Try.run(() -> Directories.deleteAll(dir.value())));
					}),

				Start.to(MongodArguments.class).initializedWith(MongodArguments.defaults()),
				MongodProcessArguments.withDefaults(),
				MongodStarter.withDefaults()
			);
	}

	public TransitionWalker.ReachedState<RunningMongodProcess> start(Version version) {
		return transitions(version)
			.walker()
			.initState(StateID.of(RunningMongodProcess.class));
	}

	public static Mongod instance() {
		return new Mongod();
	}
}
