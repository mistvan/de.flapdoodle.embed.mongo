package de.flapdoodle.embed.mongo;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.config.Defaults;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.embed.process.io.progress.ProgressListeners;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.Transitions;
import org.bson.Document;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.Date;

public class HowToUseTransitionsTest {

	@Test
	public void startMongoD() throws UnknownHostException {
		Transitions transitions = Defaults.transitionsForMongod(Version.Main.PRODUCTION);

		String dot = Transitions.edgeGraphAsDot("mongod", transitions.asGraph());
		System.out.println("---------------------");
		System.out.println(dot);
		System.out.println("---------------------");

		try (ProgressListeners.RemoveProgressListener ignored = ProgressListeners.setProgressListener(new StandardConsoleProgressListener())) {

			for (int i = 0; i < 2; i++) {
				try (TransitionWalker.ReachedState<RunningMongodProcess> running = transitions.walker()
					.initState(StateID.of(RunningMongodProcess.class))) {

					try (MongoClient mongo = new MongoClient(running.current().getServerAddress())) {
						MongoDatabase db = mongo.getDatabase("test");
						MongoCollection<Document> col = db.getCollection("testCol");
						col.insertOne(new Document("testDoc", new Date()));
						System.out.println("could store doc in database...");
					}
				}
			}
		}
	}

	
}
