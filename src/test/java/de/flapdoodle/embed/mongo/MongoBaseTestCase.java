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

import de.flapdoodle.embed.mongo.config.ImmutableMongoCmdOptions;
import de.flapdoodle.embed.mongo.config.MongoCmdOptions;
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;

/**
 * A base class for all tests which create Mongod.
 *
 * <p>It provides logic for deciding what command line options to use for
 * the different Mongo server versions.</p>
 */
public abstract class MongoBaseTestCase {
    private final ImmutableMongoCmdOptions.Builder cmdOptions = MongoCmdOptions.builder();

    protected MongoCmdOptions getCmdOptions(IFeatureAwareVersion version) {
        if (version.isNewerOrEqual(4, 2, 0)) {
            cmdOptions
                .useNoPrealloc(false)
                .useSmallFiles(false);
        }
        return cmdOptions.build();
    }

}
