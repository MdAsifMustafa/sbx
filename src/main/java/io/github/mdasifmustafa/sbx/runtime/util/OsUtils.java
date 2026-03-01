package io.github.mdasifmustafa.sbx.runtime.util;

public final class OsUtils {

    private static final String OS = System.getProperty("os.name").toLowerCase();

    private OsUtils() {}

    public static boolean isWindows() {
        return OS.contains("win");
    }
}
