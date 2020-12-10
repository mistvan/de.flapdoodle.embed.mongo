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

import java.util.EnumSet;

public class Versions {

	private Versions() {
		// no instance
	}

	public static IFeatureAwareVersion withFeatures(de.flapdoodle.embed.process.distribution.Version version, EnumSet<Feature> features) {
		return new GenericFeatureAwareVersion(version, features);
	}
	
	public static IFeatureAwareVersion withFeatures(de.flapdoodle.embed.process.distribution.Version version, Feature...features) {
		return new GenericFeatureAwareVersion(version, features);
	}
	
	static class GenericFeatureAwareVersion implements IFeatureAwareVersion {

		private final de.flapdoodle.embed.process.distribution.Version _version;
		private final EnumSet<Feature> _features;

		public GenericFeatureAwareVersion(de.flapdoodle.embed.process.distribution.Version version, EnumSet<Feature> features) {
			_version = version;
			_features = EnumSet.copyOf(features);
		}

		public GenericFeatureAwareVersion(de.flapdoodle.embed.process.distribution.Version version, Feature...features) {
			_version = version;
			_features = Feature.asSet(features);
		}
		
		@Override
		public String asInDownloadPath() {
			return _version.asInDownloadPath();
		}

		@Override
		public boolean enabled(Feature feature) {
			return _features.contains(feature);
		}

		@Override
		public EnumSet<Feature> getFeatures() {
			return _features;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((_features == null) ? 0 : _features.hashCode());
			result = prime * result + ((_version == null) ? 0 : _version.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			GenericFeatureAwareVersion other = (GenericFeatureAwareVersion) obj;
			if (_features == null) {
				if (other._features != null)
					return false;
			} else if (!_features.equals(other._features))
				return false;
			if (_version == null) {
				if (other._version != null)
					return false;
			} else if (!_version.equals(other._version))
				return false;
			return true;
		}

        @Override
        public String toString() {
           return "GenericFeatureAwareVersion{" + _version.asInDownloadPath() + "}";
		}

	}
}
