package io.github.mdasifmustafa.sbx.runtime;

public class SystemJavaDetector {

    public static int detectMajor() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            return Integer.parseInt(version.substring(2, 3));
        }
        return Integer.parseInt(version.split("\\.")[0]);
    }
}