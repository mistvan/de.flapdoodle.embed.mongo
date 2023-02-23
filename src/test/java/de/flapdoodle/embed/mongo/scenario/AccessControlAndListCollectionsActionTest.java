package de.flapdoodle.embed.mongo.scenario;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.checks.Preconditions;
import de.flapdoodle.embed.mongo.commands.MongodArguments;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.ImmutableMongod;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.embed.mongo.types.DatabaseDir;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.transitions.Start;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;

public class AccessControlAndListCollectionsActionTest {

	private final String db_name = "issue-442-test-db";
	private final String coll_name_1 = "issue-442-test-coll-1";
	private final String coll_name_2 = "issue-442-test-coll-2";

	private final String username = "issue-442-user";
	private final String pwd = "issue-442-pwd";

	private final String rolename = "issue-442-role";
	private final String action = "listCollections";

	/**
	 * see https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo/issues/442#issuecomment-1438370904
	 */
	@Test
	void createUserAndCollectionsWithoutAuthFirst(@TempDir Path temp) throws IOException {
		int port = de.flapdoodle.net.Net.freeServerPort();
		Version.Main version = Version.Main.V4_4;

		Path databaseDir = temp.resolve("database");
		Files.createDirectory(databaseDir);

		ImmutableMongod mongodWithoutAuth = Mongod.builder()
			.net(Start.to(Net.class).initializedWith(Net.defaults().withPort(port)))
			.databaseDir(Start.to(DatabaseDir.class).initializedWith(DatabaseDir.of(databaseDir)))
			.build();

		try (TransitionWalker.ReachedState<RunningMongodProcess> started = mongodWithoutAuth.start(version)) {

			ServerAddress serverAddress = getServerAddress(started);

			// mongodb-create-collections-without-auth
			try (final MongoClient without_auth = mongoClient(serverAddress)) {
				MongoDatabase database = without_auth.getDatabase(db_name);
				database.getCollection(coll_name_1).insertOne(new Document().append("field 1", "value 1"));
				database.getCollection(coll_name_2).insertOne(new Document().append("field 2", "value 2"));
			}

			// mongodb-create-user-and-role-without-auth
			try (final MongoClient without_auth = mongoClient(serverAddress)) {
				runCommand(without_auth, db_name, new Document("createRole", rolename)
					.append("privileges", Arrays.asList(
						new Document("resource",
							new Document("db", db_name)
								.append("collection", ""))
							.append("actions", Arrays.asList(action))
					))
					.append("roles", Arrays.asList()));

				runCommand(without_auth, db_name, new Document("createUser", username)
					.append("pwd", pwd)
					.append("roles", Arrays.asList(
						new Document()
							.append("role", rolename)
							.append("db", db_name)
					)));
			}

			// create admin user to call shutdown
			try (final MongoClient without_auth = mongoClient(serverAddress)) {
				runCommand(without_auth, "admin", new Document("createUser", username)
					.append("pwd", pwd)
					.append("roles", Arrays.asList("root")));
			}
		}

		ImmutableMongod mongodWithAuth = mongodWithoutAuth.withMongodArguments(Start.to(MongodArguments.class)
			.initializedWith(MongodArguments.defaults().withAuth(true)));

		try (TransitionWalker.ReachedState<RunningMongodProcess> started = mongodWithAuth.start(version)) {

			ServerAddress serverAddress = getServerAddress(started);

			// mongodb-list-collections-with-auth
			try (final MongoClient with_auth = mongoClient(serverAddress, credential(username, db_name, pwd))) {
				Document result = runCommand(with_auth,db_name, new Document("listCollections", 1));

				assertThat(result).containsEntry("ok", 1.0);
				assertThat(result)
					.extracting(doc -> doc.get("cursor"), as(MAP))
					.containsKey("firstBatch")
					.extracting(cursor -> cursor.get("firstBatch"), as(LIST))
					.hasSize(2)
					.anySatisfy(collection -> assertThat(collection)
						.isInstanceOfSatisfying(Document.class, col -> assertThat(col)
							.extracting(c -> c.get("name")).isEqualTo(coll_name_1)))
					.anySatisfy(collection -> assertThat(collection)
						.isInstanceOfSatisfying(Document.class, col -> assertThat(col)
							.extracting(c -> c.get("name")).isEqualTo(coll_name_2)));
			}
			finally {

				// shutdown db
				try (final MongoClient clientAdmin = mongoClient(serverAddress, credential(username, "admin", pwd))) {
					try {
						runCommand(clientAdmin,"admin", new Document("shutdown", 1).append("force", true));
						fail("should not reach this point");
					}
					catch (MongoSocketReadException ex) {
						assertThat(ex).hasMessageContaining("Prematurely reached end of stream");
					}

					started.current().shutDownCommandAlreadyExecuted();
				}
			}
		}
	}

	@Test
	void createUserAndCollectionsWithAuthInSingleRun(@TempDir Path temp) throws IOException {
		int port = de.flapdoodle.net.Net.freeServerPort();
		Version.Main version = Version.Main.V4_4;

		Path databaseDir = temp.resolve("database");
		Files.createDirectory(databaseDir);

		ImmutableMongod mongodWithoutAuth = Mongod.builder()
			.net(Start.to(Net.class).initializedWith(Net.defaults().withPort(port)))
//			.databaseDir(Start.to(DatabaseDir.class).initializedWith(DatabaseDir.of(databaseDir)))
			.mongodArguments(Start.to(MongodArguments.class)
				.initializedWith(MongodArguments.defaults().withAuth(true)))
			.build();

		try (TransitionWalker.ReachedState<RunningMongodProcess> started = mongodWithoutAuth.start(version)) {
			ServerAddress serverAddress = getServerAddress(started);

			// create admin user to call shutdown
			try (final MongoClient without_auth = mongoClient(serverAddress)) {
				runCommand(without_auth, "admin", new Document("createUser", username)
					.append("pwd", pwd)
					.append("roles", Arrays.asList("root")));
			}

			// mongodb-create-user-and-role-without-auth
			try (final MongoClient withadmin_auth = mongoClient(serverAddress, credential(username, "admin", pwd))) {
				runCommand(withadmin_auth, db_name, new Document("createRole", rolename)
					.append("privileges", Arrays.asList(
						new Document("resource",
							new Document("db", db_name)
								.append("collection", ""))
							.append("actions", Arrays.asList(action))
					))
					.append("roles", Arrays.asList()));

				runCommand(withadmin_auth, db_name, new Document("createUser", username)
					.append("pwd", pwd)
					.append("roles", Arrays.asList(
						new Document()
							.append("role", rolename)
							.append("db", db_name)
					)));
			}

			// mongodb-create-collections-without-auth
			try (final MongoClient withadmin_auth = mongoClient(serverAddress, credential(username, "admin", pwd))) {
				MongoDatabase database = withadmin_auth.getDatabase(db_name);
				database.getCollection(coll_name_1).insertOne(new Document().append("field 1", "value 1"));
				database.getCollection(coll_name_2).insertOne(new Document().append("field 2", "value 2"));
			}

			try (final MongoClient with_auth = mongoClient(serverAddress, credential(username, db_name, pwd))) {
				Document result = runCommand(with_auth,db_name, new Document("listCollections", 1));

				assertThat(result).containsEntry("ok", 1.0);
				assertThat(result)
					.extracting(doc -> doc.get("cursor"), as(MAP))
					.containsKey("firstBatch")
					.extracting(cursor -> cursor.get("firstBatch"), as(LIST))
					.hasSize(2)
					.anySatisfy(collection -> assertThat(collection)
						.isInstanceOfSatisfying(Document.class, col -> assertThat(col)
							.extracting(c -> c.get("name")).isEqualTo(coll_name_1)))
					.anySatisfy(collection -> assertThat(collection)
						.isInstanceOfSatisfying(Document.class, col -> assertThat(col)
							.extracting(c -> c.get("name")).isEqualTo(coll_name_2)));
			}
			finally {
				// shutdown db               k
				try (final MongoClient clientAdmin = mongoClient(serverAddress, credential(username, "admin", pwd))) {
					try {
						runCommand(clientAdmin,"admin", new Document("shutdown", 1).append("force", true));
						fail("should not reach this point");
					}
					catch (MongoSocketReadException ex) {
						assertThat(ex).hasMessageContaining("Prematurely reached end of stream");
					}

					started.current().shutDownCommandAlreadyExecuted();
				}
			}
		}
	}

	private static Document runCommand(MongoClient client, String database, Document command) {
		Document result = client.getDatabase(database).runCommand(command);
		Preconditions.checkArgument(result.containsKey("ok"),"command %s failed with: %s", command, result);
		Preconditions.checkArgument(Double.valueOf(1.0).equals(result.get("ok")),"command %s failed with: %s", command, result);
		return result;
	}

	private static MongoCredential credential(String username, String database, String password) {
		return MongoCredential.createCredential(username, database, password.toCharArray());
	}

	private static MongoClient mongoClient(ServerAddress serverAddress) {
		return new MongoClient(serverAddress);
	}

	private static MongoClient mongoClient(ServerAddress serverAddress, MongoCredential credential) {
		return new MongoClient(serverAddress, credential, MongoClientOptions.builder().build());
	}

	private static ServerAddress getServerAddress(
		final TransitionWalker.ReachedState<RunningMongodProcess> running
	) {
		final de.flapdoodle.embed.mongo.commands.ServerAddress address = running.current().getServerAddress();
		return new ServerAddress(address.getHost(), address.getPort());
	}
}
