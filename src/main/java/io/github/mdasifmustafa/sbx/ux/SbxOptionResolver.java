package io.github.mdasifmustafa.sbx.ux;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Scanner;

public final class SbxOptionResolver {

    public static String resolve(JsonNode section, Scanner scanner) {

        String prompt = section.get("prompt").asText();
        boolean allowCustom =
                section.has("allowCustom") && section.get("allowCustom").asBoolean();

        System.out.println(prompt + ":");

        for (JsonNode opt : section.get("options")) {
            System.out.println(
                    "  " + opt.get("id").asText() + ") " + opt.get("label").asText()
            );
        }

        System.out.print("Select option or type value: ");
        String input = scanner.nextLine().trim().toLowerCase();

        for (JsonNode opt : section.get("options")) {

            // numeric selection
            if (opt.get("id").asText().equals(input)) {
                return opt.get("value").asText();
            }

            // alias selection
            if (opt.has("aliases")) {
                for (JsonNode alias : opt.get("aliases")) {
                    if (alias.asText().equalsIgnoreCase(input)) {
                        return opt.get("value").asText();
                    }
                }
            }

            // direct value typing
            if (opt.get("value").asText().equalsIgnoreCase(input)) {
                return opt.get("value").asText();
            }
        }

        if (allowCustom) {
            return input;
        }

        throw new IllegalStateException(
                "Invalid selection: " + input
        );
    }

    private SbxOptionResolver() {
        // utility class
    }
}