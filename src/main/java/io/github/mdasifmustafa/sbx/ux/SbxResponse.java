package io.github.mdasifmustafa.sbx.ux;

import java.nio.file.Path;

public final class SbxResponse {

    private static final Path ROOT = Path.of(".");

    private static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String BRIGHT_RED = "\u001B[91m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_CYAN = "\u001B[96m";

    private SbxResponse() {
    }

    public static void info(String message) {
        SbxLogger.info(ROOT, message);
        System.out.println("[" + BLUE + "INFO" + RESET + "] " + message);
    }

    public static void success(String message) {
        SbxLogger.success(ROOT, message);
        System.out.println("[" + GREEN + "SUCCESS" + RESET + "] " + message);
    }

    public static void error(String message) {
        SbxLogger.error(ROOT, message);
        System.err.println("[" + RED + "ERROR" + RESET + "] " + message);
    }

    public static void warning(String message) {
        SbxLogger.warning(ROOT, message);
        System.out.println("[" + YELLOW + "WARNING" + RESET + "] " + message);
    }

    public static void help(String message) {
        SbxLogger.help(ROOT, message);
        System.out.println("[" + MAGENTA + "HELP" + RESET + "] " + message);
    }

    public static void tip(String message) {
        SbxLogger.tip(ROOT, message);
        System.out.println("[" + BRIGHT_CYAN + "TIP" + RESET + "] " + message);
    }
}
