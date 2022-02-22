package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.mongo.commands.MongoShellArguments;
import de.flapdoodle.embed.mongo.packageresolver.Command;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.Transitions;
import de.flapdoodle.reverse.transitions.Start;

public class MongoShell implements WorkspaceDefaults, VersionAndPlatform, ProcessDefaults, CommandName, ExtractFileSet {

	public Transitions transitions(de.flapdoodle.embed.process.distribution.Version version) {
		return workspaceDefaults()
			.addAll(versionAndPlatform())
			.addAll(processDefaults())
			.addAll(commandName())
			.addAll(extractFileSet())
			.addAll(
				Start.to(Command.class).initializedWith(Command.Mongo).withTransitionLabel("provide Command"),
				Start.to(de.flapdoodle.embed.process.distribution.Version.class).initializedWith(version),
				Start.to(MongoShellArguments.class).initializedWith(MongoShellArguments.defaults()),
				MongoShellProcessArguments.withDefaults(),
				ExecutedMongoShellProcess.withDefaults()
			);
	}

	public TransitionWalker.ReachedState<ExecutedMongoShellProcess> start(Version version) {
		return transitions(version)
			.walker()
			.initState(StateID.of(ExecutedMongoShellProcess.class));
	}

	public static MongoShell instance() {
		return new MongoShell();
	}

}
