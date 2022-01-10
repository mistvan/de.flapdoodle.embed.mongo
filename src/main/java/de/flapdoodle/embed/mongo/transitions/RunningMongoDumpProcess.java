package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.runtime.ProcessControl;
import de.flapdoodle.embed.process.types.RunningProcess;
import de.flapdoodle.embed.process.types.RunningProcessImpl;

import java.nio.file.Path;

public class RunningMongoDumpProcess extends RunningProcessImpl {

	public RunningMongoDumpProcess(ProcessControl process, ProcessOutput processOutput, Path pidFile, long timeout) {
		super(process, processOutput, pidFile, timeout);
	}

}
