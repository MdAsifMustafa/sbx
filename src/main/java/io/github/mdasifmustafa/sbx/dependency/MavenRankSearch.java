package io.github.mdasifmustafa.sbx.dependency;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MavenRankSearch {

    public static class Item {
        public final String groupId;
        public final String artifactId;
        public final String description;

        public Item(String g, String a, String d) {
            this.groupId = g;
            this.artifactId = a;
            this.description = d;
        }

        public String coords() {
            return groupId + ":" + artifactId;
        }
    }

    public static List<Item> search(String keyword) {
        List<Item> result = new ArrayList<>();

        try {
            String q = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

            URI uri = URI.create(
                    "https://mavenrank.sbxcli.workers.dev/search?q=" + q + "&rows=20"
            );

            URL url = uri.toURL();
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);

            if (con.getResponseCode() != 200) {
                return result;
            }

            try (InputStream is = con.getInputStream();
                 Scanner sc = new Scanner(is, StandardCharsets.UTF_8)) {

                String json = sc.useDelimiter("\\A").next();

                String[] parts = json.split("\\{");

                for (String p : parts) {
                    if (!p.contains("group_id")) continue;

                    String g = extract(p, "\"group_id\":\"");
                    String a = extract(p, "\"artifact_id\":\"");
                    String d = extract(p, "\"description\":\"");

                    if (g != null && a != null) {
                        result.add(new Item(g, a, d != null ? d : ""));
                    }
                }
            }

        } catch (Exception ignored) {}

        return result;
    }

    private static String extract(String json, String key) {
        int idx = json.indexOf(key);
        if (idx == -1) return null;
        String sub = json.substring(idx + key.length());
        return sub.substring(0, sub.indexOf("\""));
    }
}