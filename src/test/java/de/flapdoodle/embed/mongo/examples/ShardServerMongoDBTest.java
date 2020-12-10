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
package de.flapdoodle.embed.mongo.examples;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongoCmdOptions;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;

public class ShardServerMongoDBTest {

    private MongodExecutable mongodExe;
    private MongodProcess mongod;

    private MongoClient mongo;

    @Before
    public void setUp() throws Exception {

        MongodStarter runtime = MongodStarter.getDefaultInstance();
        MongodConfig config = MongodConfig.builder()
                .version(Version.Main.PRODUCTION)
                .cmdOptions(MongoCmdOptions.builder().useNoJournal(false).build())
                .isShardServer(true)
                .build();

        mongodExe = runtime.prepare(config);
        mongod = mongodExe.start();

        mongo = new MongoClient(config.net().getServerAddress().getHostName(),
                config.net().getPort());
    }

    @After
    public void tearDown() throws Exception {

        mongod.stop();
        mongodExe.stop();
    }

    public Mongo getMongo() {
        return mongo;
    }

    /*
     * Get command list options (http://docs.mongodb.org/manual/reference/command/getCmdLineOpts/)
     */
    @Test
    public void testIsShardServer() {
        DB mongoAdminDB = getMongo().getDB("admin");
        CommandResult cr = mongoAdminDB.command(new BasicDBObject(
                "getCmdLineOpts", 1));
        Object arguments = cr.get("argv");
        if (arguments instanceof BasicDBList) {
            BasicDBList argumentList = (BasicDBList) arguments;
            for (Object arg : argumentList) {
                if (arg.equals("--shardsvr")) {
                    return;
                }
            }
            fail("Could not find --shardsvr in the argument list.");
        } else {
            fail("Could not get argv from getCmdLineOpts command.");
        }
    }

}
