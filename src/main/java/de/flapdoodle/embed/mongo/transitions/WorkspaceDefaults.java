package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.mongo.types.DistributionBaseUrl;
import de.flapdoodle.embed.process.nio.directories.TempDir;
import de.flapdoodle.embed.process.transitions.InitTempDirectory;
import de.flapdoodle.embed.process.types.ProcessWorkingDir;
import de.flapdoodle.reverse.State;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.reverse.Transitions;
import de.flapdoodle.reverse.transitions.Derive;
import de.flapdoodle.reverse.transitions.Start;
import de.flapdoodle.types.Try;
import org.immutables.value.Value;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public interface WorkspaceDefaults {
	default InitTempDirectory initTempDirectory() {
		return InitTempDirectory.withPlatformTempRandomSubDir();
	}

	default Transition<ProcessWorkingDir> processWorkingDir() {
		return Derive.given(TempDir.class)
			.state(ProcessWorkingDir.class)
			.with(tempDir -> {
				Path workingDir= Try.get(() -> tempDir.createDirectory("workingDir"));
				return State.of(ProcessWorkingDir.of(workingDir), current -> {
					Try.run(() -> Files.walk(current.value())
						.sorted(Comparator.reverseOrder())
						.map(Path::toFile)
						.forEach(File::delete));
				});
			});
	}

	default Transition<DistributionBaseUrl> distributionBaseUrl() {
		return Start.to(DistributionBaseUrl.class)
			.initializedWith(DistributionBaseUrl.of("https://fastdl.mongodb.org"));
	}

	default Transitions workspaceDefaults() {
		return Transitions.from(initTempDirectory(), processWorkingDir(), distributionBaseUrl());
	}
}
