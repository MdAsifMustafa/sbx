package io.github.mdasifmustafa.sbx.dependency;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MavenCentralSearch {

    private static final Logger logger = LoggerFactory.getLogger(MavenCentralSearch.class);

    public static String resolveLatestVersion(String groupId, String artifactId) {
        try {
            String q = URLEncoder.encode(
                "g:\"" + groupId + "\" AND a:\"" + artifactId + "\"",
                StandardCharsets.UTF_8
            );

            URI uri = URI.create(
                "https://search.maven.org/solrsearch/select?q=" + q + "&rows=1&wt=json"
            );

            URL url = uri.toURL();
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);

            if (con.getResponseCode() != 200) {
                return null;
            }

            try (InputStream is = con.getInputStream();
                 Scanner scanner = new Scanner(is, StandardCharsets.UTF_8)) {

                String json = scanner.useDelimiter("\\A").next();

                if (!json.contains("\"numFound\":1")) {
                    return null;
                }

                int idx = json.indexOf("\"latestVersion\":\"");
                if (idx == -1) return null;

                String sub = json.substring(idx + 17);
                return sub.substring(0, sub.indexOf("\""));
            }

        } catch (IOException e) {
            logger.warn("Failed to resolve latest version for {}:{}", groupId, artifactId, e);
            return null;
        }
    }

    public static boolean exists(String groupId, String artifactId) {
        return resolveLatestVersion(groupId, artifactId) != null;
    }
    public static MavenArtifact resolve(String groupId, String artifactId) {
        try {
            String q = URLEncoder.encode(
                    "g:\"" + groupId + "\" AND a:\"" + artifactId + "\"",
                    StandardCharsets.UTF_8
            );

            URI uri = URI.create(
                    "https://search.maven.org/solrsearch/select?q=" + q + "&rows=1&wt=json"
            );

            URL url = uri.toURL();
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);

            if (con.getResponseCode() != 200) {
                return null;
            }

            try (InputStream is = con.getInputStream()) {

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(is);

                JsonNode response = root.get("response");

                if (response == null || response.get("numFound").asInt() == 0) {
                    return null;
                }

                JsonNode docs = response.get("docs");

                if (docs == null || docs.isEmpty()) {
                    return null;
                }

                JsonNode doc = docs.get(0);

                String version =
                        doc.has("latestVersion")
                                ? doc.get("latestVersion").asText()
                                : null;

                if (version == null) {
                    return null;
                }

                String description =
                        doc.has("description")
                                ? doc.get("description").asText()
                                : "";

                return new MavenArtifact(
                        groupId,
                        artifactId,
                        version,
                        description
                );
            }

        } catch (IOException e) {
            logger.warn("Failed to resolve artifact {}:{}", groupId, artifactId, e);
            return null;
        }
    }


}