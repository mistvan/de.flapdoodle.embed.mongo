package de.flapdoodle.embed.mongo.transitions;

import com.mongodb.ServerAddress;
import de.flapdoodle.embed.mongo.commands.ImmutableMongoDumpArguments;
import de.flapdoodle.embed.mongo.commands.MongoDumpArguments;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.io.progress.ProgressListeners;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.TransitionMapping;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.Transitions;
import de.flapdoodle.reverse.transitions.Derive;
import de.flapdoodle.reverse.transitions.Start;
import de.flapdoodle.types.Try;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class MongoDumpTest {
	private static void dump(Version.Main version, MongoDumpArguments mongoDumpArguments, Runnable afterDump) {
		try (ProgressListeners.RemoveProgressListener ignored = ProgressListeners.setProgressListener(new StandardConsoleProgressListener())) {
			Transitions transitions = MongoDump.instance().transitions(version)
				.replace(Start.to(MongoDumpArguments.class).initializedWith(mongoDumpArguments))
				.addAll(Derive.given(RunningMongodProcess.class).state(ServerAddress.class)
					.deriveBy(Try.function(RunningMongodProcess::getServerAddress).mapCheckedException(RuntimeException::new)::apply))
				.addAll(Mongod.instance().transitions(version).walker()
					.asTransitionTo(TransitionMapping.builder("mongod", StateID.of(RunningMongodProcess.class))
						.build()));

			try (TransitionWalker.ReachedState<RunningMongodProcess> runningMongoD = transitions.walker()
				.initState(StateID.of(RunningMongodProcess.class))) {

				try (TransitionWalker.ReachedState<ExecutedMongoDumpProcess> executedDump = runningMongoD.initState(
					StateID.of(ExecutedMongoDumpProcess.class))) {

					System.out.println("-------------------");
					System.out.println("dump executed: "+executedDump.current().returnCode());
					System.out.println("-------------------");
				}

				afterDump.run();
			}
		}
	}

	@Test
	public void dumpToArchive(@TempDir Path temp) {
		Path archive = temp.resolve("foo.archive.gz");

		ImmutableMongoDumpArguments mongoDumpArguments = MongoDumpArguments.builder()
			.verbose(true)
			.archive(archive.toAbsolutePath().toString())
			.gzip(true)
			.build();

		dump(Version.Main.PRODUCTION, mongoDumpArguments, () -> {
			assertThat(archive).exists()
				.isRegularFile();
		});
	}

	@Test
	public void dumpToDirectory(@TempDir Path temp) {
		Path directory = temp.resolve("dump");

		ImmutableMongoDumpArguments mongoDumpArguments = MongoDumpArguments.builder()
			.verbose(true)
			.dir(directory.toAbsolutePath().toString())
			.build();

		dump(Version.Main.PRODUCTION, mongoDumpArguments, () -> {
			assertThat(directory).exists()
				.isDirectory();
		});
	}
}