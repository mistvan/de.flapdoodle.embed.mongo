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
package de.flapdoodle.embed.mongo.distribution;

import de.flapdoodle.embed.mongo.packageresolver.FeatureSet;
import de.flapdoodle.embed.mongo.packageresolver.FeatureSetResolver;
import de.flapdoodle.embed.mongo.packageresolver.NumericVersion;

import java.util.Objects;

public class Versions {

	private Versions() {
		// no instance
	}

	public static IFeatureAwareVersion withFeatures(de.flapdoodle.embed.process.distribution.Version version) {
		return new GenericFeatureAwareVersion(version);
	}

	static class GenericFeatureAwareVersion implements IFeatureAwareVersion {

		private final de.flapdoodle.embed.process.distribution.Version _version;
		private final FeatureSet featureSet;

		public GenericFeatureAwareVersion(de.flapdoodle.embed.process.distribution.Version version) {
			_version = version;
			featureSet = FeatureSetResolver.defaultInstance().featuresOf(version);
		}

		@Override
		public String asInDownloadPath() {
			return _version.asInDownloadPath();
		}

		@Override
		public FeatureSet features() {
			return featureSet;
		}

		@Override
		public NumericVersion numericVersion() {
			throw new IllegalArgumentException("not implemented");
		}

		@Override
		public String toString() {
           return "GenericFeatureAwareVersion{" + _version.asInDownloadPath() + "}";
		}

		@Override public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			GenericFeatureAwareVersion that = (GenericFeatureAwareVersion) o;
			return _version.equals(that._version) && featureSet.equals(that.featureSet);
		}
		
		@Override public int hashCode() {
			return Objects.hash(_version, featureSet);
		}
	}
}
