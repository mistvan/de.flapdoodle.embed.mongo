package de.flapdoodle.embed.mongo.transitions;

import com.google.common.collect.ImmutableMap;
import de.flapdoodle.embed.process.nio.directories.PersistentDir;
import de.flapdoodle.reverse.State;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.StateLookup;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ExtractFileSetTest {

	@Test
	public void overridePersistentPathWithSystemEnv(@TempDir Path tempDir) {
		ExtractFileSet noEnvTestee = new ExtractFileSet() {
			@Override public Map<String, String> systemEnv() {
				return ImmutableMap.of();
			}
		};

		ExtractFileSet withEnvTestee = new ExtractFileSet() {
			@Override public Map<String, String> systemEnv() {
				return ImmutableMap.of("EMBEDDED_MONGO_ARTIFACTS", tempDir.toString());
			}
		};

		StateLookup lookupMock = new StateLookup() {
			@Override public <D> D of(StateID<D> type) {
				throw new IllegalArgumentException("should not be called");
			}
		};

		State<PersistentDir> result = noEnvTestee.persistentBaseDir().result(lookupMock);
		assertThat(result.value().value()).isNotEqualTo(tempDir);

		result = withEnvTestee.persistentBaseDir().result(lookupMock);
		assertThat(result.value().value()).isEqualTo(tempDir);
	}
}