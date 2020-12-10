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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.runtime.Mongod;
import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.ExtractedFileSet;
import de.flapdoodle.embed.process.io.directories.PropertyOrPlatformTempDir;
import de.flapdoodle.embed.process.io.file.Files;

/**
 *
 */
public class MongodProcess extends AbstractMongoProcess<MongodConfig, MongodExecutable, MongodProcess> {

	private static Logger logger = LoggerFactory.getLogger(MongodProcess.class);

	private File dbDir;
	private boolean dbDirIsTemp;

	public MongodProcess(Distribution distribution, MongodConfig config, RuntimeConfig runtimeConfig,
			MongodExecutable mongodExecutable) throws IOException {
		super(distribution, config, runtimeConfig, mongodExecutable);

	}

	@Override
	protected void onBeforeProcess(RuntimeConfig runtimeConfig) {
		super.onBeforeProcess(runtimeConfig);

		try {
			MongodConfig config = getConfig();
	
			File tmpDbDir;
			if (config.replication().getDatabaseDir() != null) {
				tmpDbDir = Files.createOrCheckDir(config.replication().getDatabaseDir());
			} else {
				tmpDbDir = Files.createTempDir(PropertyOrPlatformTempDir.defaultInstance(),"embedmongo-db");
				dbDirIsTemp = true;
			}
			this.dbDir = tmpDbDir;
		} catch (IOException iox) {
			throw new RuntimeException(iox);
		}
	}
	
	@Override
	protected void onBeforeProcessStart(ProcessBuilder processBuilder, MongodConfig config, RuntimeConfig runtimeConfig) {
		config.processListener().onBeforeProcessStart(this.dbDir,dbDirIsTemp);
		super.onBeforeProcessStart(processBuilder, config, runtimeConfig);
	}
	
	@Override
	protected void onAfterProcessStop(MongodConfig config, RuntimeConfig runtimeConfig) {
		super.onAfterProcessStop(config, runtimeConfig);
		config.processListener().onAfterProcessStop(this.dbDir,dbDirIsTemp);
	}


	@Override
	protected List<String> getCommandLine(Distribution distribution, MongodConfig config, ExtractedFileSet files) throws IOException {
		return Mongod.enhanceCommandLinePlattformSpecific(distribution, Mongod.getCommandLine(getConfig(), files, dbDir));
	}

	@Override
	protected void deleteTempFiles() {
		super.deleteTempFiles();
		
		if ((dbDir != null) && (dbDirIsTemp) && (!Files.forceDelete(dbDir))) {
			logger.warn("Could not delete temp db dir: {}", dbDir);
		}
		
	}

}
