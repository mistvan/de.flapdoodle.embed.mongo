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

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.embed.process.runtime.Network;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.transitions.Start;
import org.junit.Test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;


public class FeatureNoBindIpToLocalhostTest {

    @Test
    public void testInsert() throws UnknownHostException {
        try (TransitionWalker.ReachedState<RunningMongodProcess> running = new Mongod() {
            @Override
            public Transition<Net> net() {
                return Start.to(Net.class).providedBy(FeatureNoBindIpToLocalhostTest::getNet);
            }
        }.start(Version.V3_6_0)) {
            try (MongoClient mongo = new MongoClient(running.current().getServerAddress())) {
                DB db = mongo.getDB("test");
                DBCollection col = db.createCollection("testCol", new BasicDBObject());
                col.save(new BasicDBObject("testDoc", new Date()));
            }
        }
    }

    private static Net getNet() {
        try {
            return Net.of("localhost",
                    Network.getFreeServerPort(),
                    Network.localhostIsIPv6());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
