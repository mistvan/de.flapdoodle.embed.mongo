package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.mongo.commands.MongodArguments;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.packageresolver.Command;
import de.flapdoodle.embed.mongo.types.DatabaseDir;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.embed.process.nio.Directories;
import de.flapdoodle.embed.process.nio.directories.TempDir;
import de.flapdoodle.reverse.*;
import de.flapdoodle.reverse.transitions.Derive;
import de.flapdoodle.reverse.transitions.Start;
import de.flapdoodle.types.Try;

public class Mongod implements WorkspaceDefaults, VersionAndPlatform, ProcessDefaults, CommandName, ExtractFileSet {

	public Transition<MongodArguments> mongodArguments() {
		return Start.to(MongodArguments.class).initializedWith(MongodArguments.defaults());
	}

	public Transition<Net> net() {
		return Start.to(Net.class).providedBy(Net::defaults);
	}

	public Transition<DatabaseDir> databaseDir() {
		return Derive.given(TempDir.class).state(DatabaseDir.class)
			.with(tempDir -> {
				DatabaseDir databaseDir = Try.get(() -> DatabaseDir.of(tempDir.createDirectory("mongod-database")));
				return State.of(databaseDir, dir -> Try.run(() -> Directories.deleteAll(dir.value())));
			});
	}

	public MongodProcessArguments mongodProcessArguments() {
		return MongodProcessArguments.withDefaults();
	}

	private MongodStarter mongodStarter() {
		return MongodStarter.withDefaults();
	}

	public Transitions transitions(de.flapdoodle.embed.process.distribution.Version version) {
		return workspaceDefaults()
			.addAll(versionAndPlatform())
			.addAll(processDefaults())
			.addAll(commandName())
//			.addAll(extractedFileSetFor(StateID.of(ExtractedFileSet.class), StateID.of(Distribution.class), StateID.of(TempDir.class), StateID.of(Command.class), StateID.of(DistributionBaseUrl.class)))
			.addAll(extractFileSet())
			.addAll(
				Start.to(Command.class).initializedWith(Command.MongoD).withTransitionLabel("provide Command"),
				Start.to(de.flapdoodle.embed.process.distribution.Version.class).initializedWith(version),
				net(),

				databaseDir(),
				mongodArguments(),
				mongodProcessArguments(),
				mongodStarter()
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
