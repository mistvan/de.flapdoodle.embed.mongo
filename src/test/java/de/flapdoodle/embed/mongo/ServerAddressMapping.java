package de.flapdoodle.embed.mongo;

import de.flapdoodle.embed.mongo.commands.ServerAddress;

public class ServerAddressMapping {
	public static com.mongodb.ServerAddress serverAddress(ServerAddress serverAddress) {
		return new com.mongodb.ServerAddress(serverAddress.getHost(), serverAddress.getPort());
	}
}
