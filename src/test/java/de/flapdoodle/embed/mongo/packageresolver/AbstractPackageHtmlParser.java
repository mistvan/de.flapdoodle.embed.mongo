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
package de.flapdoodle.embed.mongo.packageresolver;

import de.flapdoodle.embed.mongo.distribution.NumericVersion;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AbstractPackageHtmlParser {

	static Set<String> namesOf(List<ParsedVersion> versions) {
		return versions.stream()
			.flatMap(it -> it.dists.stream().map(dist -> dist.name))
			.sorted(Comparator.naturalOrder())
			.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	static List<ParsedVersion> filterByName(List<ParsedVersion> versions, String name) {
		return filter(versions, it -> it.name.equals(name));
	}

	static void versionAndUrl(List<ParsedVersion> versions) {
		versions
			.stream()
			.sorted()
			.forEach(version -> {
				if (!version.dists.isEmpty()) {
					System.out.println(version.version);
					version.dists.forEach(dist -> {
						dist.urls.forEach(packageUrl -> {
							System.out.println("  " + packageUrl.url);
						});
					});
				}
			});
	}

	static void compressedVersionAndUrl(List<ParsedVersion> versions) {
		Map<String, List<ParsedVersion>> groupedByVersionLessUrl = groupByVersionLessUrl(
			versions);

		groupedByVersionLessUrl.forEach((url, versionList) -> {
			System.out.println(url.isEmpty() ? "--" : url);
			//String versionNumbers = versionList.stream().map(it -> it.version).collect(Collectors.joining(", "));
			List<String> versionNumbers = versionNumbers(versionList);

			String compressedVersions = compressedVersionAsString(versionNumbers);

			System.out.println(compressedVersions);
		});
	}

	private static String compressedVersionAsString(List<String> versionNumbers) {
		String compressedVersions = compressedVersionsList(versionNumbers)
			.stream()
			.sorted(Comparator.comparing(VersionRange::min).reversed())
			.map(r -> r.min().equals(r.max())
				? asString(r.min())
				: asString(r.max()) + " - " + asString(r.min()))
			.collect(Collectors.joining(", "));
		return compressedVersions;
	}

	static List<String> versionNumbers(List<ParsedVersion> versions) {
		return versions.stream().map(it -> it.version).collect(Collectors.toList());
	}

	static Map<String, List<ParsedVersion>> groupByVersionLessUrl(List<ParsedVersion> versions) {
		Map<String, List<ParsedVersion>> groupedByVersionLessUrl = versions.stream()
			.sorted()
			.collect(Collectors.groupingBy(version -> {
				List<String> urls = version.dists.stream()
					.flatMap(dist -> dist.urls.stream())
					.map(packageUrl -> packageUrl.url)
					.collect(Collectors.toList());

				String versionLessUrl = urls.stream().map(it -> it.replace(version.version, "{}"))
					.collect(Collectors.joining("|"));

				return versionLessUrl;
			}));
		return groupedByVersionLessUrl;
	}

	static List<VersionRange> compressedVersionsList(List<String> numericVersions) {
		List<NumericVersion> versions = numericVersions.stream().map(NumericVersion::of)
			.sorted()
			.collect(Collectors.toList());

		List<VersionRange> ranges = new ArrayList<>();
		if (!versions.isEmpty()) {
			int start=0;
			while (start<versions.size()) {
				NumericVersion min=versions.get(start);
				NumericVersion max=min;
				int maxFoundAt=start;
				for (int i=start+1;i<versions.size();i++) {
					NumericVersion current=versions.get(i);
					if (current.isNextOrPrevPatch(max)) {
						max=current;
						maxFoundAt=i;
					}
				}
				ranges.add(VersionRange.of(min, max));
				start=maxFoundAt+1;
			}
		}
		return ranges;
	}

	static String asString(NumericVersion version) {
		return version.major() + "." + version.minor() + "." + version.patch();
	}

	static void dump(List<ParsedVersion> versions) {
		versions.forEach(version -> {
			System.out.println(version.version);
			version.dists.forEach(dist -> {
				System.out.println(" " + dist.name);
				dist.urls.forEach(packageUrl -> {
					System.out.println("  " + packageUrl.url);
				});
			});
		});
	}

	static class ParsedUrl {
		final String url;

		public ParsedUrl(String url) {
			this.url = url;
		}
	}

	static class ParsedDist {
		final String name;
		final List<MongoPackageHtmlPageParser.ParsedUrl> urls;

		public ParsedDist(String name, List<MongoPackageHtmlPageParser.ParsedUrl> urls) {
			this.name = name;
			this.urls = urls;
		}
	}

	static class ParsedVersion implements Comparable<ParsedVersion> {

		final String version;
		final List<MongoPackageHtmlPageParser.ParsedDist> dists;

		public ParsedVersion(String version, List<MongoPackageHtmlPageParser.ParsedDist> dists) {
			this.version = version;
			this.dists = dists;
		}

		@Override
		public int compareTo(ParsedVersion other) {
			return -1 * NumericVersion.of(version).compareTo(NumericVersion.of(other.version));
		}
	}

	static List<MongoPackageHtmlPageParser.ParsedVersion> filter(List<MongoPackageHtmlPageParser.ParsedVersion> src,
		Predicate<MongoPackageHtmlPageParser.ParsedDist> distFilter) {
		return src.stream()
			.map(version -> {
				List<MongoPackageHtmlPageParser.ParsedDist> filtered = version.dists.stream().filter(distFilter).collect(Collectors.toList());
				return new MongoPackageHtmlPageParser.ParsedVersion(version.version, filtered);
			})
			.collect(Collectors.toList());
	}
}
