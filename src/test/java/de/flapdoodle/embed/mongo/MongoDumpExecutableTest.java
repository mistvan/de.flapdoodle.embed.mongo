/**
 * Copyright (C) 2011
 *   Michael Mosmann <michael@mosmann.de>
 *   Martin Jöhren <m.joehren@googlemail.com>
 *
 * with contributions from
 * 	konstantin-ba@github,Archimedes Trajano	(trajano@github)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.flapdoodle.embed.mongo;

import com.mongodb.ServerAddress;
import de.flapdoodle.embed.mongo.commands.ImmutableMongoDumpArguments;
import de.flapdoodle.embed.mongo.commands.MongoDumpArguments;
import de.flapdoodle.embed.mongo.config.Defaults;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.RunningMongoDumpProcess;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
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

public class MongoDumpExecutableTest {

    private static void dump(Version.Main version, MongoDumpArguments mongoDumpArguments, Runnable afterDump) {
        try (ProgressListeners.RemoveProgressListener ignored = ProgressListeners.setProgressListener(new StandardConsoleProgressListener())) {
            Transitions transitions = Defaults.transitionsForMongoDump(version)
              .replace(Start.to(MongoDumpArguments.class).initializedWith(mongoDumpArguments))
              .addAll(Derive.given(RunningMongodProcess.class).state(ServerAddress.class)
                .deriveBy(Try.function(RunningMongodProcess::getServerAddress).mapCheckedException(RuntimeException::new)::apply))
              .addAll(Defaults.transitionsForMongod(version).walker()
                .asTransitionTo(TransitionMapping.builder("mongod", StateID.of(RunningMongodProcess.class))
                  .build()));

            try (TransitionWalker.ReachedState<RunningMongodProcess> runningMongoD = transitions.walker()
              .initState(StateID.of(RunningMongodProcess.class))) {

                try (TransitionWalker.ReachedState<RunningMongoDumpProcess> runningDump = runningMongoD.initState(
                  StateID.of(RunningMongoDumpProcess.class))) {

                    System.out.println("-------------------");
                    System.out.println("dump started");
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


////    @Rule
////    public TemporaryFolder temp = new TemporaryFolder();
//    private MongodExecutable mongodExe;
//    private MongodProcess mongod;
//    private Net net;
//
//
//    @BeforeEach
//    public void setUp() throws IOException {
//        net = new Net(Network.getLocalHost().getHostAddress(),
//                Network.getFreeServerPort(),
//                Network.localhostIsIPv6());
//        final Version.Main version = Version.Main.PRODUCTION;
//        MongodConfig mongodConfig = MongodConfig.builder()
//                .version(version)
//                .cmdOptions(getCmdOptions(version))
//                .net(net)
//                .build();
//
//        RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(Command.MongoD).build();
//        mongodExe = MongodStarter.getInstance(runtimeConfig).prepare(mongodConfig);
//        mongod = mongodExe.start();
//        MongoClient mongoClient = new MongoClient(net.getServerAddress().getHostName(), net.getPort());
//        String dumpDir = Thread.currentThread().getContextClassLoader().getResource("dump").getFile();
//        mongoRestoreExecutable(dumpDir).start();
//
//        assertThat(mongoClient.getDatabase("restoredb").getCollection("sample").count(), Is.is(3L));
//    }
//
//    @AfterEach
//    public void after() {
//        mongod.stop();
//        mongodExe.stop();
//    }
//
//    @Test
//    public void testStartMongoDump(@TempDir Path temp) throws IOException {
//        final Version.Main version = Version.Main.PRODUCTION;
//        MongoDumpConfig mongoDumpConfig = MongoDumpConfig.builder()
//                .version(version)
//                .cmdOptions(getCmdOptions(version))
//                .net(net)
//                .out(temp.toAbsolutePath().toString())
//                .build();
//
//        MongoDumpStarter.getDefaultInstance().prepare(mongoDumpConfig).start();
//        assertTrue(Arrays.stream(temp.getRoot().listFiles()).anyMatch(f -> "restoredb".equals(f.getName())));
//    }
//
//    @Test
//    public void testStartMongoDumpToArchive() throws IOException {
//        final Version.Main version = Version.Main.PRODUCTION;
//        MongoDumpConfig mongoDumpConfig = MongoDumpConfig.builder()
//                .version(version)
//                .cmdOptions(getCmdOptions(version))
//                .net(net)
//                .archive(temp.getRoot().getAbsolutePath())
//                .build();
//        MongoDumpStarter.getDefaultInstance().prepare(mongoDumpConfig).start();
//
//        assertTrue(Arrays.stream(temp.getRoot().listFiles()).anyMatch(f -> "archive".equals(f.getName())));
//    }
//
//    private MongoRestoreExecutable mongoRestoreExecutable(String dumpLocation) throws IOException {
//        MongoRestoreConfig mongoRestoreConfig = MongoRestoreConfig.builder()
//                .version(Version.Main.PRODUCTION)
//                .net(net)
//                .isDropCollection(true)
//                .dir(dumpLocation)
//                .build();
//
//        return MongoRestoreStarter.getDefaultInstance().prepare(mongoRestoreConfig);
//    }
//
}
