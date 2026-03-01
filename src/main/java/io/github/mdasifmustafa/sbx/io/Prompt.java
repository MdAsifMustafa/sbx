package io.github.mdasifmustafa.sbx.io;

import java.io.Console;
import java.util.List;
import java.util.Scanner;

public final class Prompt {

    private static final Scanner scanner = new Scanner(System.in);

    public static String ask(String label, String def) {
        System.out.print(label + (def != null ? " [" + def + "]" : "") + ": ");
        String input = scanner.nextLine();
        return input.isBlank() ? def : input;
    }

    public static String askPassword(String label) {
        Console console = System.console();
        if (console != null) {
            return new String(console.readPassword(label + ": "));
        }
        System.out.print(label + ": ");
        return scanner.nextLine();
    }

    public static String choose(String label, List<String> options) {
        System.out.println(label + ":");
        for (int i = 0; i < options.size(); i++) {
            System.out.println("  " + (i + 1) + ") " + options.get(i));
        }
        while (true) {
            System.out.print("Select [1-" + options.size() + "]: ");
            int idx = Integer.parseInt(scanner.nextLine());
            if (idx >= 1 && idx <= options.size()) {
                return options.get(idx - 1);
            }
        }
    }
    public static boolean confirm(String label, boolean def) {
        String suffix = def ? "Y/n" : "y/N";
        System.out.print(label + " [" + suffix + "]: ");
        String input = scanner.nextLine().trim().toLowerCase();
        if (input.isEmpty()) return def;
        return input.startsWith("y");
    }
}