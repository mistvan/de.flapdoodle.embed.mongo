package de.flapdoodle.embed.mongo.scenario;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.commands.MongodArguments;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.ImmutableMongod;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.embed.mongo.types.DatabaseDir;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.transitions.Start;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.plaf.synth.SynthTextAreaUI;
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

	/*
mongodb_uri_without_auth="mongodb://localhost:27017/"
mongodb_uri_with_auth="mongodb://${username}:${pwd}@localhost:27017/?authMechanism=SCRAM-SHA-1&authSource=${db_name}"
 */

	/**
	 * see https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo/issues/442#issuecomment-1438370904
	 */
	@Test
	void workingSample(@TempDir Path temp) throws IOException {
		int port = de.flapdoodle.net.Net.freeServerPort();
		Version.Main version = Version.Main.V4_4;

		Path databaseDir = temp.resolve("database");
		Files.createDirectory(databaseDir);

		ImmutableMongod mongodWithoutAuth = Mongod.builder()
			.net(Start.to(Net.class).initializedWith(Net.defaults().withPort(port)))
			.databaseDir(Start.to(DatabaseDir.class).initializedWith(DatabaseDir.of(databaseDir)))
			.build();

		try (TransitionWalker.ReachedState<RunningMongodProcess> started = mongodWithoutAuth
			.start(version)) {

			ServerAddress serverAddress = getServerAddress(started);

			// mongodb-create-collections-without-auth
			try (final MongoClient without_auth = new MongoClient(serverAddress)) {
				MongoDatabase database = without_auth.getDatabase(db_name);
				database.getCollection(coll_name_1).insertOne(new Document().append("field 1", "value 1"));
				database.getCollection(coll_name_2).insertOne(new Document().append("field 2", "value 2"));
			}

			// mongodb-create-user-and-role-without-auth
			try (final MongoClient without_auth = new MongoClient(serverAddress)) {
				MongoDatabase database = without_auth.getDatabase(db_name);
				database.runCommand(new Document("createRole", rolename)
					.append("privileges", Arrays.asList(
						new Document("resource",
							new Document("db", db_name)
								.append("collection",""))
							.append("actions", Arrays.asList(action))
					))
						.append("roles", Arrays.asList()));

				database.runCommand(new Document("createUser", username)
					.append("pwd", pwd)
					.append("roles", Arrays.asList(
						new Document()
							.append("role", rolename)
							.append("db", db_name)
					)));
			}

			// create admin user to call shutdown
			try (final MongoClient without_auth = new MongoClient(serverAddress)) {
				MongoDatabase database = without_auth.getDatabase("admin");
				database.runCommand(new Document("createUser", username)
					.append("pwd", pwd)
					.append("roles", Arrays.asList("root")));
			}
		}

		ImmutableMongod mongodWithAuth = mongodWithoutAuth.withMongodArguments(Start.to(MongodArguments.class)
			.initializedWith(MongodArguments.defaults()
				.withAuth(true)));

		try (TransitionWalker.ReachedState<RunningMongodProcess> started = mongodWithAuth
			.start(version)) {

			ServerAddress serverAddress = getServerAddress(started);

			// mongodb-list-collections-with-auth
			final MongoCredential credentials =
				MongoCredential.createCredential(username, db_name, pwd.toCharArray());

			try (final MongoClient with_auth = new MongoClient(serverAddress, credentials, MongoClientOptions.builder().build())) {
				MongoDatabase database = with_auth.getDatabase(db_name);
				Document result = database.runCommand(new Document().append("listCollections", 1));

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
				final MongoCredential credentialAdmin =
					MongoCredential.createCredential(username, "admin", pwd.toCharArray());

				try (final MongoClient clientAdmin = new MongoClient(serverAddress, credentialAdmin, MongoClientOptions.builder().build())) {
					try {

						clientAdmin.getDatabase("admin").runCommand(
							new Document("shutdown", 1).append("force", true)
						);
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

	private static ServerAddress getServerAddress(
		final TransitionWalker.ReachedState<RunningMongodProcess> running
	) {
		final de.flapdoodle.embed.mongo.commands.ServerAddress address = running.current().getServerAddress();
		return new ServerAddress(address.getHost(), address.getPort());
	}
}
