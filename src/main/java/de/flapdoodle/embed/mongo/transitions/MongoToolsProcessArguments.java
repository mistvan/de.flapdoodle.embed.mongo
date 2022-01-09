package de.flapdoodle.embed.mongo.transitions;

import com.mongodb.ServerAddress;
import de.flapdoodle.embed.mongo.commands.MongoToolsArguments;
import de.flapdoodle.embed.process.types.ProcessArguments;
import de.flapdoodle.reverse.State;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.StateLookup;
import org.immutables.value.Value;

import java.util.List;
import java.util.Set;

public abstract class MongoToolsProcessArguments<T extends MongoToolsArguments> implements CommandProcessArguments<T> {
	@Override
	@Value.Default
	public StateID<ProcessArguments> destination() {
		return StateID.of(ProcessArguments.class);
	}

	@Override
	public abstract StateID<T> arguments();

	@Value.Default
	public StateID<ServerAddress> serverAddress() {
		return StateID.of(ServerAddress.class);
	}

	@Override
	@Value.Auxiliary
	public Set<StateID<?>> sources() {
		return StateID.setOf(arguments(), serverAddress());
	}

	@Override
	public State<ProcessArguments> result(StateLookup lookup) {
		T arguments = lookup.of(arguments());
		ServerAddress serverAddress = lookup.of(serverAddress());

		List<String> commandLine = arguments.asArguments(serverAddress);
		return State.of(ProcessArguments.of(commandLine));
	}
}
