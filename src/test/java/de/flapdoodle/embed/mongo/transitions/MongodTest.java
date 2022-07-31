package de.flapdoodle.embed.mongo.transitions;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.commands.MongodArguments;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.transitions.Start;
import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;

class MongodTest {
	private static final Logger logger = LoggerFactory.getLogger(MongodTest.class.getName());

	@Test
	public void testStartStopTenTimesWithNewMongoExecutable() throws IOException {
		int loops = 10;

		Mongod mongod = new Mongod() {
			@Override public Transition<MongodArguments> mongodArguments() {
				return Start.to(MongodArguments.class)
					.initializedWith(MongodArguments.defaults()
						.withUseNoPrealloc(true)
						.withUseSmallFiles(true));
			}
		};

		for (int i = 0; i < loops; i++) {
			logger.info("Loop: {}", i);
			try (TransitionWalker.ReachedState<RunningMongodProcess> runningMongod = mongod.start(Version.Main.PRODUCTION)) {
				try (MongoClient mongo = new MongoClient(runningMongod.current().getServerAddress())) {
					DB db = mongo.getDB("test");
					DBCollection col = db.createCollection("testCol", new BasicDBObject());
					col.save(new BasicDBObject("testDoc", new Date()));
				}
			}
		}
	}

	@Test
	public void startTwoMongodInstancesUsingDifferentPorts() throws UnknownHostException {
		try (TransitionWalker.ReachedState<RunningMongodProcess> outerMongod = Mongod.instance().start(Version.Main.PRODUCTION)) {
			try (TransitionWalker.ReachedState<RunningMongodProcess> innerMongod = Mongod.instance().start(Version.Main.PRODUCTION)) {

				try (MongoClient mongo = new MongoClient(innerMongod.current().getServerAddress())) {
					MongoDatabase db = mongo.getDatabase("test");
					db.createCollection("testCol");
					MongoCollection<Document> col = db.getCollection("testColl");
					col.insertOne(new Document("testDoc", new Date()));
				}

				try (MongoClient mongo = new MongoClient(outerMongod.current().getServerAddress())) {
					MongoDatabase db = mongo.getDatabase("test");
					db.createCollection("testCol");
					MongoCollection<Document> col = db.getCollection("testColl");
					col.insertOne(new Document("testDoc", new Date()));
				}
			}
		}
	}

	@Test
	public void testStartMongodOnNonFreePort() {
		Net net = Net.defaults();

		Mongod mongod = new Mongod() {
			@Override public Transition<Net> net() {
				return Start.to(Net.class)
					.initializedWith(net);
			}
		};

		try (TransitionWalker.ReachedState<RunningMongodProcess> outerMongod = mongod.start(Version.Main.PRODUCTION)) {
			Assertions.assertThatThrownBy(() -> mongod.start(Version.Main.PRODUCTION))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("error on transition to State(de.flapdoodle.embed.mongo.transitions.RunningMongodProcess), rollback");
		}
	}

}