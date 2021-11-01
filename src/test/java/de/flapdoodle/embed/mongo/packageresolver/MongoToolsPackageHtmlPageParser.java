package de.flapdoodle.embed.mongo.packageresolver;

import com.google.common.io.Resources;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MongoToolsPackageHtmlPageParser extends AbstractPackageHtmlParser {
		public static void main(String[] args) throws IOException {
				URL url = Resources.getResource("mongotools-versions.html");
				System.out.println("-> " + url);
				Document document = Jsoup.parse(Resources.toString(url, StandardCharsets.UTF_8));

				List<ParsedVersion> versions = parse(document);
//    dump(versions);
				List<String> names = namesOf(versions)
						.stream()
//            .filter(name -> supported(name))
						.sorted(Comparator.naturalOrder())
						.collect(Collectors.toList());
//    List<ParsedVersion> filtered = filter(versions, it -> it.name.contains("indows"));
				names.forEach(name -> {
						System.out.println("-----------------------------------");
						System.out.println(name);
						List<ParsedVersion> filtered = filter(versions, it -> it.name.equals(name));
						versionAndUrl(filtered);
				});

				System.out.println();
				System.out.println("-----------------------------------");
				System.out.println("- ");
				System.out.println("-----------------------------------");
				System.out.println();

				names.forEach(name -> {
						System.out.println("-----------------------------------");
						System.out.println(name);
						List<ParsedVersion> filtered = filter(versions, it -> it.name.equals(name));
						compressedVersionAndUrl(filtered);
				});
		}

		static List<ParsedVersion> parse(Document document) {
				List<ParsedVersion> versions=new ArrayList<>();
				Elements divs = document.select("div > div");
				for (Element div : divs) {
//      System.out.println("----------------");
						Element versionElement = div.selectFirst("h3");
						if (versionElement != null) {
								String version = versionElement.text();
//        System.out.println("Version: " + version);
//        System.out.println(div);
								List<ParsedDist> parsedDists=new ArrayList<>();
								Elements entries = div.select("div > ul > li");
								for (Element entry : entries) {
//          System.out.println("- - - - - - -");
										String name = entry.selectFirst("li > p").text();
//          System.out.println(" Name: " + name);
//          System.out.println(entry);
										List<ParsedUrl> parsedUrls=new ArrayList<>();
										Elements platforms = entry.select("li > ul > li");
										for (Element platform : platforms) {
//            System.out.println("~~~~~~~~");
//            System.out.println(platform);
												Elements packages = platform.select("li");
												for (Element ppackage : packages) {
														if (ppackage.text().startsWith("Archive:")) {
//                System.out.println("*********");
//                System.out.println(ppackage);
																Element urlElement = ppackage.selectFirst("a");
																String platFormUrl=urlElement.attr("href");
//                System.out.println("  Url: "+platFormUrl);
																parsedUrls.add(new ParsedUrl(platFormUrl));
														}
												}
										}
										parsedDists.add(new ParsedDist(name, parsedUrls));
								}
								versions.add(new ParsedVersion(version, parsedDists));
						} else {
//        System.out.println("##############");
//        System.out.println(div);
						}
				}
				return versions;
		}

}
