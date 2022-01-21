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
import de.flapdoodle.embed.process.config.store.DistributionPackage;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.os.OS;
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
    Set<String> names = namesOf(versions);
//    List<ParsedVersion> filtered = filter(versions, it -> it.name.contains("indows"));
    names.forEach(name -> {
      System.out.println("-----------------------------------");
      System.out.println(name);
      List<ParsedVersion> filtered = filterByName(versions, name);
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
      List<ParsedVersion> filtered = filterByName(versions, name);
      compressedVersionAndUrl(filtered);
    });

    asPlatformRules(versions);
  }

  private static void asPlatformRules(List<ParsedVersion> versions) {
    if (true) {
      PlatformMatchRules rules = asRules(versions);
      String explainedRules = ExplainRules.explain(rules);
      System.out.println("---------------------------");
      System.out.println(explainedRules);
      System.out.println("---------------------------");
    }
  }

  private static PlatformMatchRules asRules(List<ParsedVersion> versions) {
    Arrays.stream(OS.values()).forEach(os -> {
      System.out.println("os -> "+os);
      os.distributions().forEach(dist -> {
        System.out.println(" -> "+dist);
        dist.versions().forEach(version -> {
          System.out.println("  -> "+version);
        });
      });
    });
    //Arrays.stream(OS.values()).map(PlatformMatchRule.)
    ImmutablePlatformMatchRules.Builder builder = PlatformMatchRules.builder();
    for (OS os : OS.values()) {
      builder.addRules(PlatformMatchRule.of(PlatformMatch.withOs(os), packageFinderForOs(os, versions)));
    }
    return builder.build();
  }

  private static PackageFinder packageFinderForOs(OS os, List<ParsedVersion> versions) {
//    versions.forEach(v -> v.dists.forEach(d -> System.out.println(" dist "+d.name)));

    List<ParsedVersion> versionsForOs = versions.stream()
      .filter(v -> v.dists.stream().anyMatch(dist -> distMatchesOs(dist, os)))
      .collect(Collectors.toList());

    List<VersionRange> ranges = compressedVersionsList(versionNumbers(versionsForOs));

    if (os.distributions().isEmpty()) {
      return new NestedRulesPackageFinderHack(groupByPlatformAndBitSize(os, versionsForOs));
    } else {

    }
    
    return new PackageFinder() {
      @Override public Optional<DistributionPackage> packageFor(Distribution distribution) {
        return Optional.empty();
      }
    };
  }

  private static PlatformMatchRules groupByPlatformAndBitSize(OS os, List<ParsedVersion> versions) {
    Map<String, List<ParsedVersion>> groupedByUrl = groupByVersionLessUrl(versions);

    groupedByUrl.forEach((url, list) -> System.out.println("url: "+url));

    ImmutablePlatformMatchRules.Builder builder = PlatformMatchRules.builder();
    versions.forEach(v -> {

//      builder.addRules(PlatformMatchRule.of(PlatformMatch.withOs(os)
//        .andThen(VersionRange.of(v.version, v.version)), UrlTemplatePackageResolver.builder()
//          .urlTemplate(v.)
//        .build()));
    });
    return builder.build();
  }

  private static boolean distMatchesOs(ParsedDist dist, OS os) {
    switch (os) {
      case Windows:
        return dist.name.contains("indows");
      case OS_X:
        return dist.name.contains("maxOS");
      case Linux:
        return dist.name.contains("Debian");
    }
    return false;
  }

  static class NestedRulesPackageFinderHack implements PackageFinder, HasPlatformMatchRules {

    private final PlatformMatchRules rules;

    public NestedRulesPackageFinderHack(PlatformMatchRules rules) {
      this.rules = rules;
    }

    @Override
    public Optional<DistributionPackage> packageFor(Distribution distribution) {
      return rules.packageFor(distribution);
    }

    @Override
    public PlatformMatchRules rules() {
      return rules;
    }
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
}
