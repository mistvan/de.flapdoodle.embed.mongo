package de.flapdoodle.embed.mongo.packageresolver;

import com.google.common.io.Resources;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MongoPackageInfoUpdater {

	public static void main(String[] args) throws IOException {
		System.out.println("--------------------------------------");
		System.out.println("process mongodb download version files");
		System.out.println("--------------------------------------");

		processLinuxVersions(Resources.getResource("versions/linux.html"));
	}
	
	private static void processLinuxVersions(URL resource) throws IOException {
		Document document = Jsoup.parse(Resources.toString(resource, StandardCharsets.UTF_8));
		Elements rows = document.select("table > tbody > tr");

		rows.forEach(row -> {
//			System.out.print(".");
//			System.out.println(row);
			Element link = row.selectFirst("td > a");
			if (link!=null) {
				String url = link.attr("href");
				System.out.println("-> " + url);
			} else {
				System.out.println("no link: "+row);
			}
		});
		System.out.println("done");
	}
}
