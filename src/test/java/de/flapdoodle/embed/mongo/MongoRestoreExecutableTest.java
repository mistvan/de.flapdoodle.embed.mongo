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

import static de.flapdoodle.embed.mongo.TestUtils.getCmdOptions;

import java.io.IOException;

import de.flapdoodle.embed.mongo.packageresolver.Command;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.flapdoodle.embed.mongo.config.Defaults;
import de.flapdoodle.embed.mongo.config.MongoRestoreConfig;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.runtime.Network;

public class MongoRestoreExecutableTest {

   private static final Logger _logger = LoggerFactory.getLogger(MongoRestoreExecutableTest.class.getName());
   private static final String _archiveFileCompressed = "foo.archive.gz";

   @Test
   public void testStartMongoRestore() throws IOException, InterruptedException {

      final int serverPort = Network.getFreeServerPort();
      final String dumpLocation = Thread.currentThread().getContextClassLoader().getResource("dump").getFile();

      final Version.Main version = Version.Main.PRODUCTION;
      final MongodConfig mongodConfig = MongodConfig.builder()
              .version(version)
              .net(new Net(serverPort, Network.localhostIsIPv6()))
              .cmdOptions(getCmdOptions(version))
              .build();

      final RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(Command.MongoD).build();

      final MongodExecutable mongodExe = MongodStarter.getInstance(runtimeConfig).prepare(mongodConfig);
      final MongodProcess mongod = mongodExe.start();

      final MongoRestoreExecutable mongoRestoreExecutable = mongoRestoreExecutable(serverPort, dumpLocation, true);
      final MongoRestoreExecutable mongoRestoreExecutableArchive = mongoRestoreExecutableWithArchiveCompressed(serverPort, dumpLocation, true);

      MongoRestoreProcess mongoRestoreProcess = null;
      MongoRestoreProcess mongoRestoreArchiveProcess = null;

      boolean dataRestored = false;
      try {
         mongoRestoreProcess = mongoRestoreExecutable.start();
         mongoRestoreArchiveProcess = mongoRestoreExecutableArchive.start();

         dataRestored = true;

      } catch (Exception e) {
         _logger.info("MongoRestore exception: {}", e.getStackTrace());
         dataRestored = false;
      } finally {
         Assert.assertTrue("mongoDB restore data in json format", dataRestored);
         mongoRestoreProcess.stop();
         mongoRestoreArchiveProcess.stop();
      }

      mongod.stop();
      mongodExe.stop();
   }

   private MongoRestoreExecutable mongoRestoreExecutable(final int port,
                                                         final String dumpLocation,
                                                         final Boolean drop) throws IOException {

      MongoRestoreConfig mongoRestoreConfig = MongoRestoreConfig.builder()
         .version(Version.Main.PRODUCTION)
         .net(new Net(port, Network.localhostIsIPv6()))
         .isDropCollection(drop)
         .dir(dumpLocation)
         .build();

      return MongoRestoreStarter.getDefaultInstance().prepare(mongoRestoreConfig);
   }

   private MongoRestoreExecutable mongoRestoreExecutableWithArchiveCompressed(final int port,
                                                                              final String dumpLocation,
                                                                              final Boolean drop) throws IOException {

      MongoRestoreConfig mongoRestoreConfig = MongoRestoreConfig.builder()
         .version(Version.Main.PRODUCTION)
         .archive(String.format("%s/%s", dumpLocation, _archiveFileCompressed))
         .isGzip(true)
         .net(new Net(port, Network.localhostIsIPv6()))
         .isDropCollection(drop)
         .build();

      return MongoRestoreStarter.getDefaultInstance().prepare(mongoRestoreConfig);
   }
}
