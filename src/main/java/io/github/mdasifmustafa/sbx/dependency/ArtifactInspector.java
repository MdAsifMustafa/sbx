package io.github.mdasifmustafa.sbx.dependency;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.jar.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArtifactInspector {

    private static final Logger logger = LoggerFactory.getLogger(ArtifactInspector.class);

    private static final String MAVEN_CENTRAL_JAR_URL =
            "https://repo1.maven.org/maven2/%s/%s/%s/%s-%s.jar";

    private static final String PROCESSOR_SERVICE =
            "META-INF/services/javax.annotation.processing.Processor";

    public static boolean isAnnotationProcessor(
            String groupId,
            String artifactId,
            String version
    ) {

        if (version == null) {
            return false; // must know exact version to download
        }

        try {
            Path jar = downloadJar(groupId, artifactId, version);
            if (jar == null) return false;

            boolean result = containsProcessor(jar);
            Files.deleteIfExists(jar);

            return result;

        } catch (IOException e) {
            logger.debug("Failed to check if artifact is annotation processor {}:{}", groupId, artifactId, e);
            return false;
        }
    }

    private static Path downloadJar(
            String groupId,
            String artifactId,
            String version
    ) throws IOException {

        String groupPath = groupId.replace('.', '/');

        String jarUrl = String.format(
                MAVEN_CENTRAL_JAR_URL,
                groupPath,
                artifactId,
                version,
                artifactId,
                version
        );

        URL url = java.net.URI.create(jarUrl).toURL();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(5000);
        con.setReadTimeout(10000);

        if (con.getResponseCode() != 200) {
            return null;
        }

        Path temp = Files.createTempFile("artifact-", ".jar");

        try (InputStream in = con.getInputStream();
             OutputStream out = Files.newOutputStream(temp)) {

            byte[] buf = new byte[8192];
            int len;

            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
        }

        return temp;
    }

    private static boolean containsProcessor(Path jarPath)
            throws IOException {

        try (JarFile jar = new JarFile(jarPath.toFile())) {
            return jar.getEntry(PROCESSOR_SERVICE) != null;
        }
    }
}