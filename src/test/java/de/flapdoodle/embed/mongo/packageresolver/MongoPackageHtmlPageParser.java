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

import com.google.common.io.Resources;
import de.flapdoodle.types.ThrowingSupplier;
import de.flapdoodle.types.Try;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MongoPackageHtmlPageParser extends AbstractPackageHtmlParser {

  public static void main(String[] args) throws IOException {
    List<String> resources = Arrays.asList(
      "versions/react/mongo-db-versions-2021-10-28.html",
      "versions/react/mongo-db-versions-2022-01-16.html"
    );

    List<List<ParsedVersion>> allVersions = resources.stream()
      //.map(it -> Try.supplier(() -> parse(Jsoup.parse(Resources.toString(Resources.getResource(it), StandardCharsets.UTF_8)))))
      .map(it -> Try.supplier(() -> Resources.toString(Resources.getResource(it), StandardCharsets.UTF_8))
        .mapCheckedException(RuntimeException::new)
        .get())
      .map(Jsoup::parse)
      .map(MongoPackageHtmlPageParser::parse)
      .collect(Collectors.toList());

    List<ParsedVersion> versions = mergeAll(allVersions);

//    List<ParsedVersion> versions = parse(Jsoup.parse(Resources.toString(Resources.getResource("versions/react/mongo-db-versions-2021-10-28.html"), StandardCharsets.UTF_8)));

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

    asPlatformRules(versions);
  }

  private static void asPlatformRules(List<ParsedVersion> versions) {
    System.out.println("---------------------------");
    System.out.println("TODO");
    System.out.println("---------------------------");
  }

  private static List<ParsedVersion> mergeAll(List<List<ParsedVersion>> allVersions) {
    List<ParsedVersion> flatmapped = allVersions.stream().flatMap(it -> it.stream()).collect(Collectors.toList());

    Set<String> versions = flatmapped.stream()
      .map(it -> it.version)
      .collect(Collectors.toSet());

    return versions.stream().map(v -> new ParsedVersion(v, mergeDists(v, flatmapped)))
      .collect(Collectors.toList());
  }

  private static List<ParsedDist> mergeDists(String v, List<ParsedVersion> src) {
    List<ParsedDist> matchingDists = src.stream()
      .filter(pv -> v.equals(pv.version))
      .flatMap(pv -> pv.dists.stream())
      .collect(Collectors.toList());

    return groupByName(matchingDists);
  }

  private static List<ParsedDist> groupByName(List<ParsedDist> matchingDists) {
    Map<String, List<ParsedDist>> groupedByName = matchingDists.stream()
      .collect(Collectors.groupingBy(pd -> pd.name));
    
    return groupedByName.entrySet().stream()
      .map(entry -> new ParsedDist(entry.getKey(), entry.getValue().stream()
        .flatMap(it -> it.urls.stream())
        .collect(Collectors.toList())))
      .collect(Collectors.toList());
  }

//  private static List<ParsedDist> mergeDists(String v, List<List<ParsedVersion>> allVersions) {
//    matching = allVersions.stream()
//      .flatMap(all -> all.stream()
//        .filter(pv -> v.equals(pv.version))
//        .flatMap(pv -> pv.dists))
//      .collect(Collectors.toList());
//
//    return null;
//  }

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
            Elements packages = platform.select("li > p");
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

  private static boolean supported(String name) {
    switch (name) {
      case "Amazon Linux 2 ARM 64":
      case "Amazon Linux 2 x64":
      case "Amazon Linux x64":
        return false;
        
      case "Debian 10.0 x64":
      case "Debian 7.1 x64":
      case "Debian 8.1 x64":
      case "Debian 9.2 x64":
        return false;
    }
    if (name.startsWith("RedHat")) return false;
    if (name.startsWith("SUSE")) return false;
    return true;
  }
}
