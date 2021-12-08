package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.mongo.commands.CommandArguments;
import de.flapdoodle.embed.process.types.ProcessArguments;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.Transition;

public interface CommandProcessArguments<T extends CommandArguments> extends Transition<ProcessArguments> {
	StateID<T> arguments();
}
