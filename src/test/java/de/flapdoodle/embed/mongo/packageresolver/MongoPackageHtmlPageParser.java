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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.io.Resources;
import de.flapdoodle.embed.process.config.store.DistributionPackage;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.os.BitSize;
import de.flapdoodle.os.CPUType;
import de.flapdoodle.os.OS;
import de.flapdoodle.os.Version;
import de.flapdoodle.os.linux.AmazonVersion;
import de.flapdoodle.os.linux.CentosVersion;
import de.flapdoodle.os.linux.DebianVersion;
import de.flapdoodle.os.linux.UbuntuVersion;
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
import java.util.function.Function;
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

    //List<ParsedVersion> versions = mergeAll(allVersions);

    ParsedVersions versions = new ParsedVersions(mergeAll(allVersions));

//    List<ParsedVersion> versions = parse(Jsoup.parse(Resources.toString(Resources.getResource("versions/react/mongo-db-versions-2021-10-28.html"), StandardCharsets.UTF_8)));

//    dump(versions);
    Set<String> names = versions.names();
//    List<ParsedVersion> filtered = filter(versions, it -> it.name.contains("indows"));
    names.forEach(name -> {
      System.out.println("-----------------------------------");
      System.out.println(name);
      ParsedVersions filtered = versions.filterByName(name);
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
      ParsedVersions filtered = versions.filterByName(name);
      compressedVersionAndUrl(filtered);
    });

    asPlatformRules(versions);
  }

  private static void asPlatformRules(ParsedVersions versions) {
    System.out.println();
    System.out.println();
    System.out.println("- - - 8<- - - - - - - - ");
    List<PlatformVersions> byPlatform = versions.groupedByPlatform();

//    byPlatform.forEach(platformVersions -> {
//      System.out.println("----------------------------");
//      System.out.println(platformVersions.name());
//      platformVersions.urlAndVersions().forEach(urlAndVersions -> {
//        System.out.println("--");
//        System.out.println(urlAndVersions.url());
//        urlAndVersions.versions().forEach(v -> System.out.print(v+", "));
//        System.out.println();
//      });
//    });
    ImmutableListMultimap<PlatformMatch, List<UrlAndVersions>> asPlatformMatchMap = byPlatform.stream()
      .map(entry -> new Tuple<>(asPlatformMatch(entry.name()), entry.urlAndVersions()))
      .filter(tuple -> tuple.a().isPresent())
      .collect(ImmutableListMultimap.toImmutableListMultimap(tuple -> tuple.a().get(), tuple -> tuple.b()));

    asPlatformMatchMap.forEach((match, urlAndVersions) -> {
      System.out.println(ExplainRules.explainPlatformMatch(match));
      urlAndVersions.forEach(urlAndVersion -> {
        System.out.println(urlAndVersion.url()+": "+urlAndVersion.versions());
      });
    });

  }
  private static Optional<PlatformMatch> asPlatformMatch(String name) {
    Optional<OS> os=Optional.empty();
    Optional<BitSize> bitsize=Optional.empty();
    Optional<CPUType> cpuType=Optional.empty();
    Optional<Version> versions=Optional.empty();

    if (name.contains("ARM")) {
      cpuType=Optional.of(CPUType.ARM);
    }
    if (name.contains("64")) {
      bitsize=Optional.of(BitSize.B64);
    }
    if (name.contains("x64")) {
      cpuType=Optional.of(CPUType.X86);
      bitsize=Optional.of(BitSize.B64);
    }
    if (name.contains("s390x")) {
      return Optional.empty();
    }

    if (name.contains("indows")) {
      os=Optional.of(OS.Windows);
    }
    if (name.contains("Amazon Linux")) {
      os=Optional.of(OS.Linux);
      versions=Optional.of(AmazonVersion.AmazonLinux);
    }
    if (name.contains("Amazon Linux 2")) {
      os=Optional.of(OS.Linux);
      versions=Optional.of(AmazonVersion.AmazonLinux2);
    }

    if (name.contains("Debian 7.1") || name.contains("Debian 8.1") || name.contains("CentOS 5.5")) {
      return Optional.empty();
    }
    if (name.contains("Debian 10.0")) {
      os=Optional.of(OS.Linux);
      versions=Optional.of(DebianVersion.DEBIAN_10);
    }
    if (name.contains("Debian 9.2")) {
      os=Optional.of(OS.Linux);
      versions=Optional.of(DebianVersion.DEBIAN_9);
    }

    if (name.contains("CentOS 6.2") || name.contains("CentOS 6.7")) {
      os=Optional.of(OS.Linux);
      versions=Optional.of(CentosVersion.CentOS_6);
    }
    if (name.contains("CentOS 7.0")) {
      os=Optional.of(OS.Linux);
      versions=Optional.of(CentosVersion.CentOS_7);
    }
    if (name.contains("CentOS 8.0") || name.contains("CentOS 8.2")) {
      os=Optional.of(OS.Linux);
      versions=Optional.of(CentosVersion.CentOS_8);
    }

    if (name.contains("SUSE")) {
      return Optional.empty();
    }

    if (name.contains("Ubuntu 12.04") || name.contains("Ubuntu 14.04") || name.contains("Ubuntu 16.04") || name.contains("ubuntu1410-clang")) {
      return Optional.empty();
    }

    if (name.contains("Ubuntu 18.04")) {
      os=Optional.of(OS.Linux);
      versions=Optional.of(UbuntuVersion.Ubuntu_18_04);
    }
    if (name.contains("Ubuntu 20.04")) {
      os=Optional.of(OS.Linux);
      versions=Optional.of(UbuntuVersion.Ubuntu_20_04);
    }

    if (name.contains("Linux (legacy)")) {
      os=Optional.of(OS.Linux);
    }
    if (name.contains("Linux (legacy) undefined")) {
      os=Optional.of(OS.Linux);
      cpuType=Optional.of(CPUType.X86);
      bitsize=Optional.of(BitSize.B32);
    }

    if (name.contains("macOS")) {
      os=Optional.of(OS.OS_X);
    }

    if (name.contains("sunos5")) {
      os=Optional.of(OS.Solaris);
    }

    ImmutablePlatformMatch ret = PlatformMatch.builder()
      .os(os)
      .bitSize(bitsize)
      .cpuType(cpuType)
      .version(versions.map(Arrays::asList).orElse(Collections.emptyList()))
      .build();

    Preconditions.checkArgument(os.isPresent(),"no os for %s (%s)", name, ret);
    Preconditions.checkArgument(!ret.equals(PlatformMatch.any()), "could not detect %s", name);

    return Optional.of(ret);
  }

  static class Tuple<A, B> {
    private final A a;
    private final B b;
    public Tuple(A a, B b) {
      this.a = a;
      this.b = b;
    }
    public A a() {
      return a;
    }

    public B b() {
      return b;
    }
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
