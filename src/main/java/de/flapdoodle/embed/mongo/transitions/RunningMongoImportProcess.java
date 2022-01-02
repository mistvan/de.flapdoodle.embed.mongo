package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.mongo.runtime.Mongod;
import de.flapdoodle.embed.process.io.LogWatchStreamProcessor;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.StreamToLineProcessor;
import de.flapdoodle.embed.process.runtime.ProcessControl;
import de.flapdoodle.embed.process.types.RunningProcess;
import de.flapdoodle.embed.process.types.RunningProcessFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class RunningMongoImportProcess extends RunningProcess {

	private static Logger LOGGER= LoggerFactory.getLogger(RunningMongoImportProcess.class);

	public RunningMongoImportProcess(ProcessControl process, Path pidFile, long timeout) {
		super(process, pidFile, timeout);
	}
	
	public static RunningProcessFactory<RunningMongoImportProcess> factory() {
		return (process, processOutput, pidFile, timeout) -> {
			Processors.connect(process.getReader(), StreamToLineProcessor.wrap(processOutput.output()));
			Processors.connect(process.getError(), StreamToLineProcessor.wrap(processOutput.error()));

			return new RunningMongoImportProcess(process, pidFile, timeout);
		};
	}
}
