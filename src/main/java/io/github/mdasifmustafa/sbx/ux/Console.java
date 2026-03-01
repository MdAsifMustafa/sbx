package io.github.mdasifmustafa.sbx.ux;

public final class Console {

    private static final boolean ANSI;

    static {
        String os = System.getProperty("os.name").toLowerCase();

        // Windows 10+ supports ANSI. Assume true unless very old Windows.
        ANSI = !os.contains("win") || isModernWindows();
    }

    private static boolean isModernWindows() {
        try {
            String version = System.getProperty("os.version");
            return Double.parseDouble(version) >= 10;
        } catch (Exception e) {
            return true;
        }
    }

    private static String color(String code, String text) {
        return ANSI ? code + text + "\u001B[0m" : text;
    }

    public static void success(String msg) {
        System.out.println(color("\u001B[32m", "[SUCCESS]") + " " + msg);
    }

    public static void warning(String msg) {
        System.out.println(color("\u001B[33m", "[WARNING]") + " " + msg);
    }

    public static void error(String msg) {
        System.out.println(color("\u001B[31m", "[ERROR]  ") + " " + msg);
    }

    public static void info(String msg) {
        System.out.println(color("\u001B[36m", "[INFO]   ") + " " + msg);
    }
}