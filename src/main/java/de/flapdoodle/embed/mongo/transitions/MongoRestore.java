package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.mongo.commands.MongoRestoreArguments;
import de.flapdoodle.embed.mongo.packageresolver.Command;
import de.flapdoodle.embed.mongo.types.DistributionBaseUrl;
import de.flapdoodle.embed.process.archives.ExtractedFileSet;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.embed.process.nio.directories.TempDir;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.Transitions;
import de.flapdoodle.reverse.transitions.Start;

public class MongoRestore implements WorkspaceDefaults, VersionAndPlatform, ProcessDefaults, CommandName, ExtractedFileSetFor {

	public Transitions transitions(de.flapdoodle.embed.process.distribution.Version version) {
		return workspaceDefaults()
			.addAll(versionAndPlatform())
			.addAll(processDefaults())
			.addAll(commandName())
			.addAll(extractedFileSetFor(StateID.of(ExtractedFileSet.class), StateID.of(Distribution.class), StateID.of(TempDir.class), StateID.of(Command.class), StateID.of(
				DistributionBaseUrl.class)))
			.addAll(
				Start.to(Command.class).initializedWith(Command.MongoRestore).withTransitionLabel("provide Command"),
				Start.to(de.flapdoodle.embed.process.distribution.Version.class).initializedWith(version),
				Start.to(MongoRestoreArguments.class).initializedWith(MongoRestoreArguments.defaults()),
				MongoRestoreProcessArguments.withDefaults(),
				ExecutedMongoRestoreProcess.withDefaults()
			);
	}

	public TransitionWalker.ReachedState<ExecutedMongoRestoreProcess> start(Version version) {
		return transitions(version)
			.walker()
			.initState(StateID.of(ExecutedMongoRestoreProcess.class));
	}

	public static MongoRestore instance() {
		return new MongoRestore();
	}
}
