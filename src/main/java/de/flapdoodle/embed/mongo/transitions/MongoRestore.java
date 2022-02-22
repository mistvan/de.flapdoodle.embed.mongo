package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.mongo.commands.MongoRestoreArguments;
import de.flapdoodle.embed.mongo.packageresolver.Command;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.Transitions;
import de.flapdoodle.reverse.transitions.Start;

public class MongoRestore implements WorkspaceDefaults, VersionAndPlatform, ProcessDefaults, CommandName, ExtractFileSet {

	public Transitions transitions(de.flapdoodle.embed.process.distribution.Version version) {
		return workspaceDefaults()
			.addAll(versionAndPlatform())
			.addAll(processDefaults())
			.addAll(commandName())
			.addAll(extractFileSet())
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
