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
package de.flapdoodle.embed.mongo.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class Mongod {

	private static final Logger LOGGER = LoggerFactory.getLogger(Mongod.class);

	/**
	 * Binary sample of shutdown command - legacy version
	 */
	private static final byte[] SHUTDOWN_COMMAND_LEGACY = { 0x58, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
 			(byte) 0xD4, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x61, 0x64, 0x6D, 0x69, 0x6E, 0x2E, 0x24, 0x63, 0x6D, 
 			0x64, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x2C, 0x00, 0x00, 
 			0x00, 0x10, 0x73, 0x68, 0x75, 0x74, 0x64, 0x6F, 0x77, 0x6E, 0x00, 0x01, 0x00, 0x00, 0x00, 0x08, 0x66, 0x6F, 
 			0x72, 0x63, 0x65, 0x00, 0x01, 0x10, 0x74, 0x69, 0x6D, 0x65, 0x6F, 0x75, 0x74, 0x53, 0x65, 0x63, 0x73, 0x00, 
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x05, 0x00, 0x00, 0x00, 0x00 };

	/**
	 * Binary sample of shutdown command - force shutdown
	 */
	private static final byte[] SHUTDOWN_COMMAND_FORCE = { 0x63, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00,
		0x00, 0x00, 0x00, 0x00, (byte) 0xdd, 0x07, 0x00, 0x00,
		0x00, 0x00, 0x00, 0x00, 0x00, 0x4e, 0x00, 0x00,
		0x00, 0x10, 0x73, 0x68, 0x75, 0x74, 0x64, 0x6f,
		0x77, 0x6e, 0x00, 0x01, 0x00, 0x00, 0x00, 0x08,
		0x66, 0x6f, 0x72, 0x63, 0x65, 0x00, 0x01, 0x02,
		0x24, 0x64, 0x62, 0x00, 0x06, 0x00, 0x00, 0x00,
		0x61, 0x64, 0x6d, 0x69, 0x6e, 0x00, 0x03, 0x6c,
		0x73, 0x69, 0x64, 0x00, 0x1e, 0x00, 0x00, 0x00,
		0x05, 0x69, 0x64, 0x00, 0x10, 0x00, 0x00, 0x00,
		0x04, 0x31, (byte) 0xc9, 0x5e, 0x6e, (byte) 0xbf, 0x40, 0x49,
		0x75, (byte) 0xb3, (byte) 0x8b, (byte) 0x97, 0x4a, (byte) 0xb7, 0x5f, (byte) 0xcb,
		(byte) 0xa4, 0x00, 0x00 };

	/**
	 * Binary sample of shutdown command
	 */
	private static final byte[] SHUTDOWN_COMMAND = { 0x5b, 0x00, 0x00, 0x00, 0x07, 0x00, 0x00, 0x00,
		0x00, 0x00, 0x00, 0x00, (byte) 0xdd, 0x07, 0x00, 0x00,
		0x00, 0x00, 0x00, 0x00, 0x00, 0x46, 0x00, 0x00,
		0x00, 0x10, 0x73, 0x68, 0x75, 0x74, 0x64, 0x6f,
		0x77, 0x6e, 0x00, 0x01, 0x00, 0x00, 0x00, 0x02,
		0x24, 0x64, 0x62, 0x00, 0x06, 0x00, 0x00, 0x00,
		0x61, 0x64, 0x6d, 0x69, 0x6e, 0x00, 0x03, 0x6c,
		0x73, 0x69, 0x64, 0x00, 0x1e, 0x00, 0x00, 0x00,
		0x05, 0x69, 0x64, 0x00, 0x10, 0x00, 0x00, 0x00,
		0x04, 0x48, (byte) 0xec, (byte) 0xcb, (byte) 0x9e, 0x76, 0x5e, 0x4c,
		0x3d, (byte) 0x84, 0x51, (byte) 0xf9, 0x39, 0x27, 0x3f, (byte) 0x8d,
		(byte) 0xf8, 0x00, 0x00};

	private static final int SOCKET_TIMEOUT = 2000;
	private static final int CONNECT_TIMEOUT = 2000;
	private static final int BYTE_BUFFER_LENGTH = 512;
	private static final int WAITING_TIME_SHUTDOWN_IN_MS = 100;

	public static boolean sendShutdown(InetAddress hostname, int port) {
		return sendShutdown(hostname, port, SHUTDOWN_COMMAND);
	}

	public static boolean sendShutdownLegacy(InetAddress hostname, int port) {
		return sendShutdown(hostname, port, SHUTDOWN_COMMAND_LEGACY);
	}
	
	public static boolean sendShutdown(InetAddress hostname, int port, byte[] commandBinaryStream) {
		if (!hostname.isLoopbackAddress()) {
			LOGGER.warn("---------------------------------------\n"
					+ "Your localhost ({}) is not a loopback adress\n"
					+ "We can NOT send shutdown to mongod, because it is denied from remote.\n"
					+ "---------------------------------------\n", hostname.getHostAddress());
			return false;
		}

		boolean tryToReadErrorResponse = false;

		final Socket s = new Socket();
		try {
			s.setSoTimeout(SOCKET_TIMEOUT);
			s.connect(new InetSocketAddress(hostname, port), CONNECT_TIMEOUT);
			OutputStream outputStream = s.getOutputStream();
			outputStream.write(commandBinaryStream);
			outputStream.flush();

			tryToReadErrorResponse = true;
			InputStream inputStream = s.getInputStream();
			if (inputStream.read(new byte[BYTE_BUFFER_LENGTH]) != -1) {
				LOGGER.error("Got some response, should be an error message");
				return false;
			}
			return true;
		} catch (IOException iox) {
			if (tryToReadErrorResponse) {
				return true;
			}
			LOGGER.warn("sendShutdown {}:{}", hostname, port, iox);
		} finally {
			try {
				s.close();
				Thread.sleep(WAITING_TIME_SHUTDOWN_IN_MS);
			} catch (InterruptedException | IOException ix) {
				LOGGER.warn("sendShutdown closing {}:{}", hostname, port, ix);
			}
		}
		return false;
	}

	public static int getMongodProcessId(String output, int defaultValue) {
		Pattern pattern = Pattern.compile("MongoDB starting : pid=([1234567890]+) port", Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(output);
		if (matcher.find()) {
			String value = matcher.group(1);
			return Integer.parseInt(value);
		}
		return defaultValue;
	}
}
