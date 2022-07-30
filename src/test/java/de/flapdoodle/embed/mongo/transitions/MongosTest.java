package de.flapdoodle.embed.mongo.transitions;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.commands.ImmutableMongodArguments;
import de.flapdoodle.embed.mongo.commands.MongodArguments;
import de.flapdoodle.embed.mongo.commands.MongosArguments;
import de.flapdoodle.embed.mongo.config.Storage;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.transitions.Start;
import de.flapdoodle.types.Try;
import org.assertj.core.api.Assertions;
import org.bson.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class MongosTest {

	@Test
	public void clusterSample(@TempDir Path databaseDir) throws UnknownHostException {
		Version version= de.flapdoodle.embed.mongo.distribution.Version.Main.PRODUCTION;

		ImmutableMongodArguments configServerArguments = MongodArguments.defaults()
			.withIsConfigServer(true)
			.withReplication(new Storage(databaseDir.resolve("replicaSet").toString(), "ReplicaSetOne", 0));

		ImmutableMongodArguments shardServerArguments = MongodArguments.defaults()
			.withIsShardServer(true)
			.withUseNoJournal(false)
			.withReplication(new Storage(databaseDir.resolve("shardSet").toString(), "ShardSetOne", 0));

		try (TransitionWalker.ReachedState<RunningMongodProcess> configServerOne = startMongod(version, configServerArguments)) {
			try (TransitionWalker.ReachedState<RunningMongodProcess> configServerTwo = startMongod(version, configServerArguments)) {

				rsInitiate("ReplicaSetOne", configServerOne.current().getServerAddress(), configServerTwo.current().getServerAddress());

				try (TransitionWalker.ReachedState<RunningMongodProcess> shardServerOne = startMongod(version, shardServerArguments)) {
					try (TransitionWalker.ReachedState<RunningMongodProcess> shardServerTwo = startMongod(version, shardServerArguments)) {

					}
				}
			}
		}
	}
	private void rsInitiate(String replicaSetName, ServerAddress one, ServerAddress ... others) {
		List<Document> members=new ArrayList<>();
		int idx=0;
		members.add(new Document(ImmutableMap.of("_id",idx++,"host",one.toString())));
		for (ServerAddress other : others) {
			members.add(new Document(ImmutableMap.of("_id",idx++,"host",other.toString())));
		}

		try (MongoClient client = new MongoClient(one)) {
			MongoDatabase adminDB = client.getDatabase("admin");
			Document result = adminDB.runCommand(new Document("replSetInitiate", new Document(ImmutableMap.of(
				"_id", replicaSetName,
				"configsvr", true,
				"members", members
			))));

			assertThat(result.getDouble("ok")).isGreaterThan(0.9);
		}
	}

	@Test
	@Disabled
	public void replicaSetSample() {
		Version version= de.flapdoodle.embed.mongo.distribution.Version.Main.PRODUCTION;
		MongosArguments config=MongosArguments.defaults();

		Storage storage = new Storage(null, "ReplicaSetOne", 0);

		List<MongodArguments> oneReplicaSet= ImmutableList.of(
				MongodArguments.defaults().withReplication(storage).withUseNoJournal(false),
				MongodArguments.defaults().withReplication(storage).withUseNoJournal(false),
				MongodArguments.defaults().withReplication(storage).withUseNoJournal(false)
		);

		List<MongodArguments> configServers=ImmutableList.of(MongodArguments.defaults()
			.withIsConfigServer(true)
		);

		String shardDatabase="shardDB";
		String shardCollection="shardCollection";
		String shardKey="shardKey";

		List<TransitionWalker.ReachedState<RunningMongodProcess>> runningInstances = startMongodInstances(version, oneReplicaSet);

		try {
			initReplicaSet("ReplicaSetOne", runningInstances.get(0), runningInstances);

			List<TransitionWalker.ReachedState<RunningMongodProcess>> runningConfigServers = startConfigServers(version, configServers);
			try {
				System.out.println("-------------------------------");
				System.out.println("-------------------------------");
				try (TransitionWalker.ReachedState<RunningMongosProcess> runningMongos = Mongos.instance()
					.transitions(version)
					.replace(Start.to(MongosArguments.class).initializedWith(MongosArguments.defaults()
						.withConfigDB(configDbArgOf(runningConfigServers))
						.withReplicaSet("testRepSet")))
					.walker().initState(StateID.of(RunningMongosProcess.class))) {
		//		configureMongos();
				}
				System.out.println("-------------------------------");
				System.out.println("-------------------------------");
			} finally {
				runningConfigServers.forEach(TransitionWalker.ReachedState::close);
			}
			
		} finally {
			runningInstances.forEach(TransitionWalker.ReachedState::close);
		}
	}
	private String configDbArgOf(List<TransitionWalker.ReachedState<RunningMongodProcess>> runningConfigServers) {
		return runningConfigServers.stream()
			.map(it -> Try.get(it.current()::getServerAddress))
			.map(it -> it.getHost()+":"+it.getPort())
			.collect(Collectors.joining(","));
	}

	private static List<TransitionWalker.ReachedState<RunningMongodProcess>> startMongodInstances(Version version, List<MongodArguments> mongoConfigList) {
		Preconditions.checkArgument(mongoConfigList.size()>=3,"A replica set must contain at least 3 members.");

		List<TransitionWalker.ReachedState<RunningMongodProcess>> runningMongodInstances = mongoConfigList.stream()
			.map(config -> startMongod(version, config))
			.collect(Collectors.toList());

		return runningMongodInstances;
	}

	private static TransitionWalker.ReachedState<RunningMongodProcess> startMongod(Version version, MongodArguments config) {
		return Mongod.instance().transitions(version).replace(Start.to(MongodArguments.class).initializedWith(config))
			.walker()
			.initState(StateID.of(RunningMongodProcess.class));
	}

	private static TransitionWalker.ReachedState<RunningMongosProcess> startMongos(Version version, MongosArguments config) {
		return Mongos.instance().transitions(version).replace(Start.to(MongosArguments.class).initializedWith(config))
			.walker()
			.initState(StateID.of(RunningMongosProcess.class));
	}

	private static List<TransitionWalker.ReachedState<RunningMongodProcess>> startConfigServers(Version version, List<MongodArguments> mongoConfigList) {
		return mongoConfigList.stream()
			.map(it -> startConfigServer(version, it))
			.collect(Collectors.toList());
	}
	private static TransitionWalker.ReachedState<RunningMongodProcess> startConfigServer(Version version, MongodArguments config) {
		Preconditions.checkArgument(config.isConfigServer(), "Mongo configuration is not a defined for a config server.");
		return startMongod(version, config);
	}



	private static void initReplicaSet(String replicaName, TransitionWalker.ReachedState<RunningMongodProcess> master, List<TransitionWalker.ReachedState<RunningMongodProcess>> runningMongodInstances) {
		TransitionWalker.ReachedState<RunningMongodProcess> firstRunningMongoInstance = master;
		ServerAddress serverAddress = Try.get(firstRunningMongoInstance.current()::getServerAddress);

		Try.run(() -> Thread.sleep(1000));

		try (MongoClient client = new MongoClient(serverAddress)) {
			MongoDatabase mongoAdminDB = client.getDatabase("admin");

			Document cr = mongoAdminDB.runCommand(new BsonDocument("ismaster", new BsonBoolean(true)));
			assertThat(cr.getBoolean("ismaster")).isEqualTo(false);

			// Build BSON object replica set settings
			BsonDocument replicaSetSetting = new BsonDocument();
			replicaSetSetting.put("_id", new BsonString(replicaName));

			BsonArray members=new BsonArray();


			int i = 0;
			for (TransitionWalker.ReachedState<RunningMongodProcess> runningInstance : runningMongodInstances) {
				ServerAddress instanceServerAdress = Try.get(runningInstance.current()::getServerAddress);
				BsonDocument host = new BsonDocument();
				host.put("_id", new BsonInt32(i++));
				host.put("host", new BsonString(instanceServerAdress.getHost() + ":" + instanceServerAdress.getPort()));
				members.add(host);
			}

			replicaSetSetting.put("members", members);

			cr = mongoAdminDB.runCommand(new BsonDocument("replSetInitiate", replicaSetSetting));
			assertThat(cr.getDouble("ok")).isGreaterThan(0.9);

			Try.run(() -> Thread.sleep(5000));

			cr = mongoAdminDB.runCommand(new BsonDocument("replSetGetStatus", new BsonInt32(1)));

			assertThat(isReplicaSetStarted(cr)).isTrue();
		}
	}

	private static boolean isReplicaSetStarted(Document setting) {
		if (setting.get("members") == null) {
			return false;
		}

		List<?> members = (List<?>) setting.get("members");
		for (Object m : members) {
			Document member = (Document) m;
			int state = member.getInteger("state");
			// 1 - PRIMARY, 2 - SECONDARY, 7 - ARBITER
			if (state != 1 && state != 2 && state != 7) {
				return false;
			}
		}
		return true;
	}

//
//	private void initializeMongos() throws Exception {
//		de.flapdoodle.embed.mongo.MongosStarter runtime = MongosStarter.getInstance(Defaults.runtimeConfigFor(Command.MongoS, logger)
//			.build());
//
//		mongosExecutable = runtime.prepare(config);
//		mongosProcess = mongosExecutable.start();
//	}
//
//	private void configureMongos() throws Exception {
//		CommandResult cr;
//		MongoClientOptions options = MongoClientOptions.builder()
//			.connectTimeout(10)
//			.build();
//		try (MongoClient mongo = new MongoClient(
//			new ServerAddress(this.config.net().getServerAddress()
//				.getHostName(), this.config.net().getPort()), options)) {
//			DB mongoAdminDB = mongo.getDB(ADMIN_DATABASE_NAME);
//
//			// Add shard from the replica set list
//			for (Map.Entry<String, List<MongodConfig>> entry : this.replicaSets
//				.entrySet()) {
//				String replicaName = entry.getKey();
//				StringBuilder command = new StringBuilder();
//				for (MongodConfig mongodConfig : entry.getValue()) {
//					if (command.length() == 0) {
//						command = new StringBuilder(replicaName + "/");
//					} else {
//						command.append(",");
//					}
//					command.append(mongodConfig.net().getServerAddress().getHostName()).append(":").append(mongodConfig.net().getPort());
//				}
//				logger.info("Execute add shard command: {}", command.toString());
//				cr = mongoAdminDB.command(new BasicDBObject("addShard", command.toString()));
//				logger.info(cr.toString());
//			}
//
//			logger.info("Execute list shards.");
//			cr = mongoAdminDB.command(new BasicDBObject("listShards", 1));
//			logger.info(cr.toString());
//
//			// Enabled sharding at database level
//			logger.info("Enabled sharding at database level");
//			cr = mongoAdminDB.command(new BasicDBObject("enableSharding",
//				this.shardDatabase));
//			logger.info(cr.toString());
//
//			// Create index in sharded collection
//			logger.info("Create index in sharded collection");
//			DB db = mongo.getDB(this.shardDatabase);
//			db.getCollection(this.shardCollection).createIndex(this.shardKey);
//
//			// Shard the collection
//			logger.info("Shard the collection: {}.{}", this.shardDatabase, this.shardCollection);
//			DBObject cmd = new BasicDBObject();
//			cmd.put("shardCollection", this.shardDatabase + "." + this.shardCollection);
//			cmd.put("key", new BasicDBObject(this.shardKey, 1));
//			cr = mongoAdminDB.command(cmd);
//			logger.info(cr.toString());
//
//			logger.info("Get info from config/shards");
//			DBCursor cursor = mongo.getDB("config").getCollection("shards").find();
//			while (cursor.hasNext()) {
//				DBObject item = cursor.next();
//				logger.info(item.toString());
//			}
//		}
//
//	}
//
//	public Mongo getMongo() throws UnknownHostException, MongoException {
//		return new MongoClient(new ServerAddress(mongosProcess.getConfig().net()
//			.getServerAddress(), mongosProcess.getConfig().net().getPort()));
//	}
//
//	public void stop() {
//		for (MongodProcess process : this.mongodProcessList) {
//			process.stop();
//		}
//		for (MongodProcess process : this.mongodConfigProcessList) {
//			process.stop();
//		}
//		this.mongosProcess.stop();
//	}

}