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

import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.packageresolver.Feature;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.os.*;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.transitions.Start;
import org.junit.Assume;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class MongoDBRuntimeTest {

	private static List<Arguments> testableDistributions() {
		ArrayList<Arguments> ret = new ArrayList<>();
		for (OS os : OS.values()) {
			// there is no generic linux version of mongodb after 3.6
			// so we should use linux dists instead
			for (CommonArchitecture arc : CommonArchitecture.values()) {
				for (Version.Main version : Versions.testableVersions(Version.Main.class)) {
					ret.add(Arguments.of(os,arc,version));
				}
			}
		}
		return ret;
	}

	@ParameterizedTest
	@MethodSource("testableDistributions")
	public void extractArtifact(OS os, CommonArchitecture arch, Version.Main version) {
		if (skipThisVersion(os, version, arch.bitSize())) {
			Assume.assumeTrue(true);
		}	else {
			assertCanExtractArtifact(distributionOf(version, os, arch));
		}
	}

	private static void assertCanExtractArtifact(Distribution distribution) {
		try (TransitionWalker.ReachedState<de.flapdoodle.embed.process.archives.ExtractedFileSet> extractedFileSet = new Mongod() {
			@Override
			public Transition<Distribution> distribution() {
				return Start.to(Distribution.class).initializedWith(distribution);
			}
		}.transitions(distribution.version())
			.walker()
			.initState(StateID.of(de.flapdoodle.embed.process.archives.ExtractedFileSet.class))) {
			assertNotNull(extractedFileSet.current().executable());
		}
	}

	private static boolean skipThisVersion(OS os, IFeatureAwareVersion version, BitSize bitsize) {
		if (version.enabled(Feature.ONLY_64BIT) && bitsize==BitSize.B32) {
			return true;
		}
		
		if ((os == OS.OS_X) && (bitsize == BitSize.B32)) {
			// there is no osx 32bit version for v2.2.1 and above, so we dont check
			return true;
		}
		if ((os == OS.Solaris)  && ((bitsize == BitSize.B32) || version.enabled(Feature.NO_SOLARIS_SUPPORT))) {
			return true;
		}
		if (os == OS.FreeBSD) {
			return true;
		}
		if (os == OS.Windows) {
			// there is no windows 2008 version for 1.8.5
			return version.asInDownloadPath().equals(Version.V1_8_5.asInDownloadPath());
		}
		return false;
	}

	private static Distribution distributionOf(IFeatureAwareVersion version, OS os, Architecture architecture) {
		return Distribution.of(version, ImmutablePlatform.builder()
			.operatingSystem(os)
			.architecture(architecture)
			.build());
	}
}
