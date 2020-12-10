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

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;


public class FeatureNoBindIpToLocalhostTest {

    private static MongodStarter mongodStarter = MongodStarter.getDefaultInstance();
    private static Net net = getNet();
    private static MongodConfig mongodConfig = createMongoConfig(net);

    private MongodExecutable mongodExecutable;
    private MongodProcess mongodProcess;


    @Before
    public void setUp() {
        mongodExecutable = mongodStarter.prepare(mongodConfig);

        try {
            mongodProcess = mongodExecutable.start();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    @After
    public void tearDown() {
        if (mongodExecutable != null && mongodProcess != null && mongodProcess.isProcessRunning()) {
            mongodProcess.stop();
            mongodExecutable.stop();
        }
    }

    @Test
    public void testInsert() throws UnknownHostException {
        MongoClient mongo = new MongoClient(new ServerAddress(net.getServerAddress(), net.getPort()));
        DB db = mongo.getDB("test");
        DBCollection col = db.createCollection("testCol", new BasicDBObject());
        col.save(new BasicDBObject("testDoc", new Date()));
    }

    private static MongodConfig createMongoConfig(Net net) {
        return MongodConfig.builder()
		        .version(Version.V3_6_0)
		        .net(net)
		        .build();
    }

    private static Net getNet() {
        try {
            return new Net("localhost",
                    Network.getFreeServerPort(),
                    Network.localhostIsIPv6());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
