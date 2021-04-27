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
