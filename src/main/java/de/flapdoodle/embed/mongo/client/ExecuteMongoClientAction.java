/*
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
package de.flapdoodle.embed.mongo.client;

import com.mongodb.MongoCredential;
import de.flapdoodle.embed.mongo.commands.ServerAddress;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import org.bson.Document;

import java.io.Closeable;
import java.io.IOException;

public abstract class ExecuteMongoClientAction<C extends Closeable> {
	public void execute(RunningMongodProcess runningMongodProcess, MongoClientAction action) {
		try (C client = action.credentials()
			.map(c -> client(runningMongodProcess.getServerAddress(),
				MongoCredential.createCredential(c.username(), c.database(), c.password().toCharArray())))
			.orElseGet(() -> client(runningMongodProcess.getServerAddress()))) {

			action.onResult()
				.accept(resultOfAction(client, action.action()));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		catch (RuntimeException rx) {
			action.onError().accept(rx);
		}
	}

	protected abstract C client(ServerAddress serverAddress);

	protected abstract C client(ServerAddress serverAddress, MongoCredential credential);

	protected abstract Document resultOfAction(C client, MongoClientAction.Action action);
}
