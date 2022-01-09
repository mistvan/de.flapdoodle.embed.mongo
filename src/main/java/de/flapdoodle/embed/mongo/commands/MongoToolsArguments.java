package de.flapdoodle.embed.mongo.commands;

import com.mongodb.ServerAddress;

import java.util.List;

public interface MongoToolsArguments {
	List<String> asArguments(ServerAddress serverAddress);
}
