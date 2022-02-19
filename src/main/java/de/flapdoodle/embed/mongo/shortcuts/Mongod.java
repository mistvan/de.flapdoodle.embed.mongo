package de.flapdoodle.embed.mongo.shortcuts;

import de.flapdoodle.embed.mongo.config.Defaults;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.Transitions;

public abstract class Mongod {

	public static TransitionWalker.ReachedState<RunningMongodProcess> start(Version version) {
		Transitions transitions = Defaults.transitionsForMongod(version);
		return transitions
			.walker()
			.initState(StateID.of(RunningMongodProcess.class));
	}
}
