/**
 * Copyright (C) 2011
 *   Can Yaman <can@yaman.me>
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

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.config.Defaults;
import de.flapdoodle.embed.mongo.config.MongoImportConfig;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.runtime.Network;
import junit.framework.TestCase;

/**
 * Created by canyaman on 10/04/14.
 */
public class MongoImportExecutableTest extends TestCase {

    @Test
    public void testStartMongoImport() throws IOException, InterruptedException {

        Net net = new Net(Network.getFreeServerPort(), Network.localhostIsIPv6());
        MongodConfig mongodConfig = MongodConfig.builder().version(Version.Main.PRODUCTION).net(net).build();

        RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(Command.MongoD).build();

        MongodExecutable mongodExe = MongodStarter.getInstance(runtimeConfig).prepare(mongodConfig);
        MongodProcess mongod = mongodExe.start();

        File jsonFile = new File(Thread.currentThread().getContextClassLoader().getResource("primer-dataset.json").getFile());
        String importDatabase = "importDatabase";
        String importCollection = "importCollection";
        MongoImportExecutable mongoImportExecutable = mongoImportExecutable(net.getPort(), importDatabase,
                importCollection, jsonFile.getAbsolutePath(), true, true, true);
        MongoClient mongoClient = new MongoClient(net.getServerAddress().getHostName(), net.getPort());
        MongoImportProcess mongoImportProcess = mongoImportExecutable.start();
        //            everything imported?
        assertEquals(5000, mongoClient.getDatabase(importDatabase).getCollection(importCollection).count());
        mongoImportProcess.stop();

        mongod.stop();
        mongodExe.stop();
    }

    @Test
    public void testMongoImportDoesNotStopMainMongodProcess() throws IOException, InterruptedException {

        MongodConfig mongodConfig = MongodConfig.builder().version(Version.Main.PRODUCTION).net(new Net(12346, Network.localhostIsIPv6())).build();

        RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(Command.MongoD).build();

        MongodExecutable mongodExe = MongodStarter.getInstance(runtimeConfig).prepare(mongodConfig);
        MongodProcess mongod = mongodExe.start();

        File jsonFile = new File(Thread.currentThread().getContextClassLoader().getResource("sample.json").getFile());

        MongoImportExecutable mongoImportExecutable = mongoImportExecutable(12346, "importDatabase", "importCollection",
                jsonFile.getAbsolutePath(), true, true, true);
        MongoImportProcess mongoImportProcess = null;

        try {
            mongoImportProcess = mongoImportExecutable.start();
            mongoImportProcess.stop();
        } finally {
            Assert.assertTrue("mongoDB process should still be running", mongod.isProcessRunning());
        }

        mongod.stop();
        mongodExe.stop();
    }

    private MongoImportExecutable mongoImportExecutable(int port, String dbName, String collection, String jsonFile, Boolean jsonArray, Boolean upsert, Boolean drop) throws
            IOException {
        MongoImportConfig mongoImportConfig = MongoImportConfig.builder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(port, Network.localhostIsIPv6()))
                .databaseName(dbName)
                .collectionName(collection)
                .isUpsertDocuments(upsert)
                .isDropCollection(drop)
                .isJsonArray(jsonArray)
                .importFile(jsonFile)
                .build();

        return MongoImportStarter.getDefaultInstance().prepare(mongoImportConfig);
    }

}
