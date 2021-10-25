package de.flapdoodle.embed.mongo.packageresolver;

import com.google.common.io.Resources;
import de.flapdoodle.embed.mongo.distribution.NumericVersion;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MongoPackageHtmlPageParser {

  public static void main(String[] args) throws IOException {
    URL url = Resources.getResource("mongo-db-versions.html");
    System.out.println("-> "+url);
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

  private static Set<String> namesOf(List<ParsedVersion> versions) {
    return versions.stream().flatMap(it -> it.dists.stream().map(dist -> dist.name)).collect(Collectors.toSet());
  }


  private static void versionAndUrl(List<ParsedVersion> versions) {
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

  private static void compressedVersionAndUrl(List<ParsedVersion> versions) {
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

  private static String compressedVersions(List<String> numericVersions) {
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

  private static String asString(NumericVersion version) {
    return version.major()+"."+version.minor()+"."+ version.patch();
  }

  private static void dump(List<ParsedVersion> versions) {
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

  private static List<ParsedVersion> parse(Document document) {
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

  static class ParsedUrl {
    final String url;

    public ParsedUrl(String url) {
      this.url = url;
    }
  }

  static class ParsedDist {
    final String name;
    final List<ParsedUrl> urls;

    public ParsedDist(String name, List<ParsedUrl> urls) {
      this.name = name;
      this.urls = urls;
    }
  }

  static class ParsedVersion {

    final String version;
    final List<ParsedDist> dists;

    public ParsedVersion(String version, List<ParsedDist> dists) {
      this.version = version;
      this.dists = dists;
    }
  }

  static List<ParsedVersion> filter(List<ParsedVersion> src, Predicate<ParsedDist> distFilter) {
    return src.stream()
            .map(version -> {
              List<ParsedDist> filtered = version.dists.stream().filter(distFilter).collect(Collectors.toList());
              return new ParsedVersion(version.version, filtered);
            })
            .collect(Collectors.toList());
  }
}
