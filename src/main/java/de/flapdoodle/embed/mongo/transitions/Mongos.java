package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.mongo.commands.MongosArguments;
import de.flapdoodle.embed.mongo.config.Net;
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

public class Mongos implements WorkspaceDefaults, VersionAndPlatform, ProcessDefaults, CommandName, ExtractedFileSetFor {
	public Transitions transitions(de.flapdoodle.embed.process.distribution.Version version) {
		return workspaceDefaults()
			.addAll(versionAndPlatform())
			.addAll(processDefaults())
			.addAll(commandName())
			.addAll(extractedFileSetFor(StateID.of(ExtractedFileSet.class), StateID.of(Distribution.class), StateID.of(TempDir.class), StateID.of(Command.class), StateID.of(
				DistributionBaseUrl.class)))
			.addAll(
				Start.to(Command.class).initializedWith(Command.MongoS).withTransitionLabel("provide Command"),
				Start.to(de.flapdoodle.embed.process.distribution.Version.class).initializedWith(version),
				Start.to(Net.class).providedBy(Net::defaults),

				Start.to(MongosArguments.class).initializedWith(MongosArguments.defaults()),
				MongosProcessArguments.withDefaults(),
				MongosStarter.withDefaults()
			);
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
