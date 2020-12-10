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
package de.flapdoodle.embed.mongo.distribution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.EnumSet;

import org.junit.Test;

public class VersionsTest {

	@Test
	public void testEquals() {
		assertEquals(Versions.withFeatures(genericVersion("2.6.5")), Versions.withFeatures(genericVersion("2.6.5")));
		assertEquals(Versions.withFeatures(genericVersion("2.6.5"), Feature.TEXT_SEARCH), Versions.withFeatures(genericVersion("2.6.5"), Feature.TEXT_SEARCH));
		assertNotEquals(Versions.withFeatures(genericVersion("2.6.5")), Versions.withFeatures(genericVersion("2.6.5"), Feature.TEXT_SEARCH));
	}

	private de.flapdoodle.embed.process.distribution.Version genericVersion(String version) {
		return de.flapdoodle.embed.process.distribution.Version.of(version);
	}

	@Test
    public void toStringOfGenericVersion() {
      String version = "9.6.9";
      IFeatureAwareVersion iFeatureAwareVersion = Versions.withFeatures(genericVersion(version));

      assertThat(iFeatureAwareVersion.toString()).contains(version);
      assertThat(iFeatureAwareVersion.asInDownloadPath()).isEqualTo(version);
    }

	@Test
	public void getFeatures() {
		EnumSet<Feature> features = Version.Main.PRODUCTION.getFeatures();
		IFeatureAwareVersion iFeatureAwareVersion = Versions.withFeatures(genericVersion("6.6.6"), features);

		assertThat(iFeatureAwareVersion.getFeatures()).containsAll(features);
	}

}
