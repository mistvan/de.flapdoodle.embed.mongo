package de.flapdoodle.embed.mongo.packageresolver;

import de.flapdoodle.embed.mongo.distribution.NumericVersion;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AbstractPackageHtmlParser {

		static Set<String> namesOf(List<ParsedVersion> versions) {
				return versions.stream().flatMap(it -> it.dists.stream().map(dist -> dist.name)).collect(Collectors.toSet());
		}


		static void versionAndUrl(List<ParsedVersion> versions) {
				versions.forEach(version -> {
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
				Map<String, List<ParsedVersion>> groupedByVersionLessUrl = versions.stream().collect(Collectors.groupingBy(version -> {
						List<String> urls = version.dists.stream()
								.flatMap(dist -> dist.urls.stream())
								.map(packageUrl -> packageUrl.url)
								.collect(Collectors.toList());

						String versionLessUrl = urls.stream().map(it -> it.replace(version.version, "{}"))
								.collect(Collectors.joining("|"));

						return versionLessUrl;
				}));

				groupedByVersionLessUrl.forEach((url, versionList) -> {
						System.out.println(url.isEmpty() ? "--" : url);
						//String versionNumbers = versionList.stream().map(it -> it.version).collect(Collectors.joining(", "));
						List<String> versionNumbers = versionList.stream().map(it -> it.version).collect(Collectors.toList());
						System.out.println(compressedVersions(versionNumbers));
				});
		}

		static String compressedVersions(List<String> numericVersions) {
				List<NumericVersion> versions = numericVersions.stream().map(it -> NumericVersion.of(it)).collect(Collectors.toList());
				StringBuilder sb=new StringBuilder();
				for (int i = 0, versionsSize = versions.size(); i < versionsSize; i++) {
						NumericVersion version = versions.get(i);
						if (i>0) {
								NumericVersion prev=versions.get(i-1);
								boolean isLast = (i + 1) == versionsSize;
								if (version.isNextOrPrevPatch(prev)) {
										if (isLast) {
												sb.append(" - ").append(asString(version));
										}
								} else {
										sb.append(" - ").append(asString(prev)).append(", ").append(asString(version));
								}
						} else {
								sb.append(asString(version));
						}
				}
				return sb.toString();
		}

		static String asString(NumericVersion version) {
				return version.major()+"."+version.minor()+"."+ version.patch();
		}

		static void dump(List<ParsedVersion> versions) {
				versions.forEach(version -> {
						System.out.println(version.version);
						version.dists.forEach(dist -> {
								System.out.println(" "+dist.name);
								dist.urls.forEach(packageUrl -> {
										System.out.println("  "+packageUrl.url);
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

		static class ParsedVersion {

				final String version;
				final List<MongoPackageHtmlPageParser.ParsedDist> dists;

				public ParsedVersion(String version, List<MongoPackageHtmlPageParser.ParsedDist> dists) {
						this.version = version;
						this.dists = dists;
				}
		}

		static List<MongoPackageHtmlPageParser.ParsedVersion> filter(List<MongoPackageHtmlPageParser.ParsedVersion> src, Predicate<MongoPackageHtmlPageParser.ParsedDist> distFilter) {
				return src.stream()
						.map(version -> {
								List<MongoPackageHtmlPageParser.ParsedDist> filtered = version.dists.stream().filter(distFilter).collect(Collectors.toList());
								return new MongoPackageHtmlPageParser.ParsedVersion(version.version, filtered);
						})
						.collect(Collectors.toList());
		}
}
