package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.mongo.packageresolver.Command;
import de.flapdoodle.embed.process.types.Name;
import de.flapdoodle.reverse.Transitions;
import de.flapdoodle.reverse.transitions.Derive;

public interface CommandName {
	default Transitions commandName() {
		return Transitions.from(
			Derive.given(Command.class).state(Name.class).deriveBy(c -> Name.of(c.commandName())).withTransitionLabel("name from command")
		);
	}

}
