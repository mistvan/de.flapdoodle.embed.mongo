package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.mongo.commands.MongosArguments;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.packageresolver.Command;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.Transitions;
import de.flapdoodle.reverse.transitions.ImmutableStart;
import de.flapdoodle.reverse.transitions.Start;

public class Mongos implements WorkspaceDefaults, VersionAndPlatform, ProcessDefaults, CommandName, ExtractFileSet {
	public Transitions transitions(de.flapdoodle.embed.process.distribution.Version version) {
		return workspaceDefaults()
			.addAll(versionAndPlatform())
			.addAll(processDefaults())
			.addAll(commandName())
			.addAll(extractFileSet())
			.addAll(
				Start.to(Command.class).initializedWith(Command.MongoS).withTransitionLabel("provide Command"),
				Start.to(de.flapdoodle.embed.process.distribution.Version.class).initializedWith(version),
				Start.to(Net.class).providedBy(Net::defaults),

				mongosArguments(),
				MongosProcessArguments.withDefaults(),
				MongosStarter.withDefaults()
			);
	}

	public Start<MongosArguments> mongosArguments() {
		return Start.to(MongosArguments.class).initializedWith(MongosArguments.defaults());
	}

	public TransitionWalker.ReachedState<RunningMongosProcess> start(Version version) {
		return transitions(version)
			.walker()
			.initState(StateID.of(RunningMongosProcess.class));
	}

	public static Mongos instance() {
		return new Mongos();
	}


}
