package io.github.mdasifmustafa.sbx.template.dto;

public final class DtoTemplate {

    private DtoTemplate() {}

    public static String generate(
            String pkg,
            String name,
            boolean record,
            boolean validation
    ) {
        StringBuilder sb = new StringBuilder();

        sb.append("package ").append(pkg).append(";\n\n");

        if (validation) {
            sb.append("import jakarta.validation.constraints.*;\n\n");
        }

        if (record) {
            sb.append("public record ").append(name).append("(\n");
            if (validation) {
                sb.append("    @NotNull String id\n");
            } else {
                sb.append("    String id\n");
            }
            sb.append(") {}\n");
        } else {
            sb.append("public class ").append(name).append(" {\n\n");
            if (validation) {
                sb.append("    @NotNull\n");
            }
            sb.append("    private String id;\n\n");
            sb.append("}\n");
        }

        return sb.toString();
    }
}