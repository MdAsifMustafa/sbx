package io.github.mdasifmustafa.sbx.template.dto;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates DTOs from a JPA entity source file.
 *
 * Supports:
 *  - Request / Response DTOs
 *  - Lombok (@Data)
 *  - Java records
 */
public final class EntityDtoTemplate {

    private EntityDtoTemplate() {}

    public static String generate(
            String pkg,
            String dtoName,
            String entityName,
            boolean request,
            boolean response,
            boolean record,
            boolean lombok
    ) {
        if (record && lombok) {
            throw new IllegalArgumentException("Record and Lombok cannot be used together");
        }

        Path entityPath = resolveEntityPath(pkg, entityName);
        List<Field> fields = parseFields(entityPath, request);

        StringBuilder sb = new StringBuilder();

        // package
        sb.append("package ").append(pkg).append(";\n\n");

        // imports / annotations
        if (lombok) {
            sb.append("import lombok.Data;\n\n");
            sb.append("@Data\n");
        }

        // record
        if (record) {
            sb.append("public record ").append(dtoName).append("(\n");
            for (int i = 0; i < fields.size(); i++) {
                Field f = fields.get(i);
                sb.append("    ").append(f.type).append(" ").append(f.name);
                if (i < fields.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append(") {}\n");
            return sb.toString();
        }

        // class
        sb.append("public class ").append(dtoName).append(" {\n\n");

        for (Field f : fields) {
            for (String ann : f.annotations) {
                sb.append("    ").append(ann).append("\n");
            }
            sb.append("    private ").append(f.type).append(" ").append(f.name).append(";\n\n");
        }

        sb.append("}\n");
        return sb.toString();
    }

    // ---------------------------------------------------------------------
    // Parsing helpers
    // ---------------------------------------------------------------------

    private static Path resolveEntityPath(String dtoPkg, String entityName) {
        return Path.of(
                "src/main/java",
                dtoPkg
                        .replace(".api.dto", ".domain." + entityName.toLowerCase())
                        .replace(".", "/"),
                entityName + ".java"
        );
    }

    private static List<Field> parseFields(Path entityPath, boolean request) {
        try {
            List<String> lines = Files.readAllLines(entityPath);

            List<Field> fields = new ArrayList<>();
            List<String> pendingAnnotations = new ArrayList<>();

            boolean insideClass = false;
            int braceDepth = 0;

            for (String raw : lines) {
                String line = raw.trim();

                // detect class start
                if (!insideClass && line.matches(".*\\bclass\\b.*\\{?")) {
                    insideClass = true;
                    if (line.contains("{")) braceDepth++;
                    continue;
                }

                if (!insideClass) continue;

                // track braces to detect class scope
                if (line.contains("{")) braceDepth++;
                if (line.contains("}")) braceDepth--;

                // exit if class closed
                if (braceDepth <= 0) break;

                // collect annotations (multi-line safe)
                if (line.startsWith("@")) {
                    pendingAnnotations.add(line);
                    continue;
                }

                // detect field declaration
                if (line.matches("private\\s+.*;")) {

                    // skip relationships completely
                    if (hasRelationAnnotation(pendingAnnotations)) {
                        pendingAnnotations.clear();
                        continue;
                    }

                    String declaration = line
                            .replace("private", "")
                            .replace("final", "")
                            .replace(";", "")
                            .trim();

                    // remove multiple spaces
                    declaration = declaration.replaceAll("\\s+", " ");

                    int lastSpace = declaration.lastIndexOf(' ');
                    if (lastSpace == -1) {
                        pendingAnnotations.clear();
                        continue;
                    }

                    String type = declaration.substring(0, lastSpace).trim();
                    String name = declaration.substring(lastSpace + 1).trim();

                    // skip id for request DTO
                    if (request && isIdField(pendingAnnotations)) {
                        pendingAnnotations.clear();
                        continue;
                    }

                    Field field = new Field(type, name);
                    field.annotations.addAll(copyFieldAnnotations(pendingAnnotations));

                    pendingAnnotations.clear();
                    fields.add(field);
                }
            }

            return fields;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse entity: " + entityPath, e);
        }
    }
    private static boolean hasRelationAnnotation(List<String> annotations) {
        for (String a : annotations) {
            if (a.startsWith("@OneTo")
                    || a.startsWith("@ManyTo")
                    || a.startsWith("@OneToOne")
                    || a.startsWith("@ManyToMany")
                    || a.startsWith("@JoinColumn")
                    || a.startsWith("@JoinColumns")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isIdField(List<String> annotations) {
        for (String a : annotations) {
            if (a.startsWith("@Id")) {
                return true;
            }
        }
        return false;
    }

    private static List<String> copyFieldAnnotations(List<String> annotations) {
        List<String> result = new ArrayList<>();
        for (String a : annotations) {

            // skip JPA structural annotations
            if (a.startsWith("@Id")
                    || a.startsWith("@Column")
                    || a.startsWith("@GeneratedValue")
                    || a.startsWith("@Table")
                    || a.startsWith("@Entity")) {
                continue;
            }

            result.add(a);
        }
        return result;
    }

    // ---------------------------------------------------------------------
    // Model
    // ---------------------------------------------------------------------

    private static final class Field {
        final String type;
        final String name;
        final List<String> annotations = new ArrayList<>();

        Field(String type, String name) {
            this.type = type;
            this.name = name;
        }
    }
}