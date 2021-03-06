

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static String randomWikipedia = "https://en.wikipedia.org/wiki/Special:Random";
    public static String toFindWikipedia = "https://en.wikipedia.org/wiki/Poland";
    //\/wiki\/(.*\w)
    public static String wikiHrefToFind = "/Poland";
    public static String httpHeader = "https://en.wikipedia.org/wiki";
    public static String foundLink = "";
    public static Set<String> newHrefs;
    public static Set<String> subHrefs = new HashSet<>();

    public static String getHTML(String urlToRead) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }

    public static Set<String> getHrefs(String randomWikiPage) throws Exception {
        String getHTMLResult = getHTML(randomWikiPage);
        //pattern to find groups of <a> tag where group 0 is whole, group 1 is first " sign and group 2 is href value
        Pattern p = Pattern.compile("<a\\s+(?:[^>]*?\\s+)?href=([\"'])\\/wiki(.*?)\\1");
        Matcher m = p.matcher(getHTMLResult);
        Set<String> matches = new HashSet<>();
        while (m.find()) {
            matches.add(m.group(2));
        }
        return matches;
    }

    public static void lookHref(Set<String> hrefs) throws Exception {
        for (String href : hrefs) {
            if (!href.contains(".") && !href.contains(":") && !href.contains("&")) {
                newHrefs = getHrefs(httpHeader + href);
                subHrefs.addAll(newHrefs);
                foundLink = newHrefs.stream().filter(link -> link.equals(wikiHrefToFind)).findFirst().orElse("");
                if (!foundLink.equals("")) break;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int levels = 0;
        Instant start = Instant.now();
        Set<String> hrefs = getHrefs(randomWikipedia);
        System.out.println("Links on wiki " + hrefs.toString());
        foundLink = hrefs.stream().filter(link -> link.equals(wikiHrefToFind)).findFirst().orElse("");
        while (foundLink.equals("")) {
            System.out.println("LEVEL: " + levels);
            lookHref(hrefs);
            levels++;
            hrefs = new HashSet<>(subHrefs);
        }
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).getSeconds();
        System.out.println("FOUND HREF: " + foundLink + " LEVELS : "+ levels + " TIME TAKEN : " + timeElapsed + "s");


    }
}
