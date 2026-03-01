package io.github.mdasifmustafa.sbx.dependency;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenCentralValidator {

    private static final Logger logger = LoggerFactory.getLogger(MavenCentralValidator.class);

    public static boolean exists(String groupId, String artifactId) {
        try {
            String path = groupId.replace('.', '/')
                    + "/" + artifactId;
            URI uri = URI.create("https://repo1.maven.org/maven2/" + path + "/");
            URL url =uri.toURL();

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("HEAD");
            con.setConnectTimeout(3000);
            con.setReadTimeout(3000);

            return con.getResponseCode() == 200;
        } catch (IOException e) {
            logger.debug("Failed to check artifact existence for {}:{}", groupId, artifactId, e);
            return false;
        }
    }
}