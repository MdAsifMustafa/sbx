package io.github.mdasifmustafa.sbx.template.migration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class FlywayMigrationTemplate {

    private FlywayMigrationTemplate() {}

    public static String generate(String name) {
        return ""
            + "-- Migration: " + name + "\n"
            + "-- Generated at: "
            + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            + "\n\n"
            + "-- Write your SQL here\n";
    }
}