package de.flapdoodle.embed.mongo.commands;

import org.immutables.value.Value;

import java.net.InetAddress;

@Value.Immutable
public abstract class ServerAddress {
	@Value.Default
	public String getHost() {
		return defaultHost();
	}

	@Value.Default
	public int getPort() {
		return defaultPort();
	}

	@Override
	@Value.Auxiliary
	public String toString() {
		return getHost() + ":" + getPort();
	}

	public static ImmutableServerAddress.Builder builder() {
		return ImmutableServerAddress.builder();
	}

	public static String defaultHost() {
		return "127.0.0.1";
	}

	public static int defaultPort() {
		return 27017;
	}

	public static ServerAddress of(InetAddress serverAddress, int port) {
		return of(serverAddress.getHostName(), port);
	}

	public static ServerAddress of(final String host, final int port) {
			String hostToUse = host;
			if (hostToUse == null) {
				hostToUse = defaultHost();
			}
			hostToUse = hostToUse.trim();
			if (hostToUse.length() == 0) {
				hostToUse = defaultHost();
			}
			int portToUse = port;

			if (hostToUse.startsWith("[")) {
				int idx = host.indexOf("]");
				if (idx == -1) {
					throw new IllegalArgumentException("an IPV6 address must be encosed with '[' and ']'"
						+ " according to RFC 2732.");
				}

				int portIdx = host.indexOf("]:");
				if (portIdx != -1) {
					if (port != defaultPort()) {
						throw new IllegalArgumentException("can't specify port in construct and via host");
					}
					portToUse = Integer.parseInt(host.substring(portIdx + 2));
				}
				hostToUse = host.substring(1, idx);
			} else {
				int idx = hostToUse.indexOf(":");
				int lastIdx = hostToUse.lastIndexOf(":");
				if (idx == lastIdx && idx > 0) {
					if (port != defaultPort()) {
						throw new IllegalArgumentException("can't specify port in construct and via host");
					}
					try {
						portToUse = Integer.parseInt(hostToUse.substring(idx + 1));
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException("host and port should be specified in host:port format");
					}
					hostToUse = hostToUse.substring(0, idx).trim();
				}
			}
			return builder()
				.host(hostToUse.toLowerCase())
				.port(portToUse)
				.build();
	}
}
