package de.flapdoodle.embed.mongo.transitions;

import com.google.common.collect.ImmutableMap;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.commands.ImmutableMongodArguments;
import de.flapdoodle.embed.mongo.commands.ImmutableMongosArguments;
import de.flapdoodle.embed.mongo.commands.MongodArguments;
import de.flapdoodle.embed.mongo.commands.MongosArguments;
import de.flapdoodle.embed.mongo.config.Storage;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.transitions.Start;
import org.bson.*;
import org.junit.jupiter.api.Test;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class MongosTest {

	/**
	 * code for https://www.mongodb.com/docs/manual/tutorial/deploy-shard-cluster/
	 */
	@Test
	public void clusterSample() throws UnknownHostException {
		Version version= de.flapdoodle.embed.mongo.distribution.Version.Main.PRODUCTION;

		String configServerReplicaSetName = "ConfigServerSet";
		String shardReplicaSetName = "ShardSet";

		ImmutableMongodArguments configServerArguments = MongodArguments.defaults()
			.withIsConfigServer(true)
			.withReplication(Storage.of(configServerReplicaSetName, 0));

		ImmutableMongodArguments shardServerArguments = MongodArguments.defaults()
			.withIsShardServer(true)
			.withUseNoJournal(false)
			.withReplication(Storage.of(shardReplicaSetName, 0));

		try (TransitionWalker.ReachedState<RunningMongodProcess> configServerOne = startMongod("mongod#c1",version, configServerArguments)) {
			try (TransitionWalker.ReachedState<RunningMongodProcess> configServerTwo = startMongod("mongod#c2", version, configServerArguments)) {

				ServerAddress configServerOneAdress = configServerOne.current().getServerAddress();
				ServerAddress configServerTwoAdress = configServerTwo.current().getServerAddress();

				assertThat(rsInitiate(configServerReplicaSetName, true, configServerOneAdress, configServerTwoAdress)).isTrue();

				try (TransitionWalker.ReachedState<RunningMongodProcess> shardServerOne = startMongod("mongod#s1", version, shardServerArguments)) {
					try (TransitionWalker.ReachedState<RunningMongodProcess> shardServerTwo = startMongod("mongod#s1", version, shardServerArguments)) {

						assertThat(rsInitiate(shardReplicaSetName, false, shardServerOne.current().getServerAddress(), shardServerTwo.current().getServerAddress()))
							.isTrue();

						ImmutableMongosArguments mongosConfigClusterArguments = MongosArguments.defaults()
							.withReplicaSet(configServerReplicaSetName)
							.withConfigDB(Stream.of(configServerOneAdress, configServerTwoAdress)
								.map(ServerAddress::toString)
								.collect(Collectors.joining(",")));

						try (TransitionWalker.ReachedState<RunningMongosProcess> mongosConfigClusterServer = startMongos("mongos#c", version, mongosConfigClusterArguments)) {

							try(MongoClient client = new MongoClient(mongosConfigClusterServer.current().getServerAddress())) {
								MongoDatabase adminDb = client.getDatabase("admin");
								Document result = adminDb.runCommand(new Document(ImmutableMap.of(
									"addShard", shardReplicaSetName + "/" + shardServerOne.current().getServerAddress() + "," + shardServerTwo.current().getServerAddress()
									)));

								assertThat(result.getDouble("ok")).isGreaterThanOrEqualTo(1.0);
							}
						}
					}
				}
			}
		}
	}

	private boolean rsInitiate(String replicaSetName, boolean configServer, ServerAddress one, ServerAddress ... others) {
		List<Document> members=new ArrayList<>();
		int idx=0;
		members.add(new Document(ImmutableMap.of("_id",idx++,"host",one.toString())));
		for (ServerAddress other : others) {
			members.add(new Document(ImmutableMap.of("_id",idx++,"host",other.toString())));
		}

		try (MongoClient client = new MongoClient(one)) {
			MongoDatabase adminDB = client.getDatabase("admin");
			Document result = adminDB.runCommand(new Document("replSetInitiate", new Document(
				configServer
					? ImmutableMap.of("_id", replicaSetName, "configsvr", true, "members", members)
					: ImmutableMap.of("_id", replicaSetName, "members", members)
			)));

			Double ok = result.getDouble("ok");
			return Double.valueOf(1.0).compareTo(ok)>=0;
		}
	}

	private static TransitionWalker.ReachedState<RunningMongodProcess> startMongod(String id, Version version, MongodArguments config) {
		return Mongod.instance().transitions(version)
			.replace(Start.to(MongodArguments.class).initializedWith(config))
			.replace(Start.to(ProcessOutput.class).initializedWith(ProcessOutput.namedConsole(id)))
			.walker()
			.initState(StateID.of(RunningMongodProcess.class));
	}

	private static TransitionWalker.ReachedState<RunningMongosProcess> startMongos(String id, Version version, MongosArguments config) {
		return Mongos.instance().transitions(version)
			.replace(Start.to(MongosArguments.class).initializedWith(config))
			.replace(Start.to(ProcessOutput.class).initializedWith(ProcessOutput.namedConsole(id)))
			.walker()
			.initState(StateID.of(RunningMongosProcess.class));
	}
}