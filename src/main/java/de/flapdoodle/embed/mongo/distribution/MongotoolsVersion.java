package de.flapdoodle.embed.mongo.distribution;

public enum MongotoolsVersion implements de.flapdoodle.embed.process.distribution.Version {

		V100_5_1("100.5.1");

		private final String specificVersion;
		private final NumericVersion numericVersion;

		MongotoolsVersion(String version) {
				this.specificVersion = version;
				this.numericVersion = NumericVersion.of(version);
		}

		@Override
		public String asInDownloadPath() {
				return specificVersion;
		}

		public enum Main implements de.flapdoodle.embed.process.distribution.Version {
				V100_5(MongotoolsVersion.V100_5_1);

				private final MongotoolsVersion dumpVersion;
				Main(MongotoolsVersion dumpVersion) {
						this.dumpVersion = dumpVersion;
				}

				@Override
				public String asInDownloadPath() {
						return dumpVersion.asInDownloadPath();
				}
		}
}
