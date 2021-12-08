package de.flapdoodle.embed.mongo.types;

import de.flapdoodle.embed.process.types.Wrapped;
import de.flapdoodle.embed.process.types.Wrapper;
import org.immutables.value.Value;

import java.nio.file.Path;

@Value.Immutable
@Wrapped
public abstract class _DatabaseDir extends Wrapper<Path> {
}
