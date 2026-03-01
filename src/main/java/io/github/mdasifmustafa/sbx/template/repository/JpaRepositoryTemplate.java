package io.github.mdasifmustafa.sbx.template.repository;

public final class JpaRepositoryTemplate {

    private JpaRepositoryTemplate() {}

    public static String generate(
            String pkg,
            String entity,
            boolean custom,
            String query
    ) {
        StringBuilder sb = new StringBuilder();

        sb.append("package ").append(pkg).append(";\n\n");

        if (!custom) {
            sb.append("import org.springframework.data.jpa.repository.JpaRepository;\n\n");
            sb.append("public interface ")
              .append(entity).append("Repository ")
              .append("extends JpaRepository<")
              .append(entity).append(", Long> {\n\n");
        } else {
            sb.append("public interface ")
              .append(entity).append("Repository {\n\n");
        }

        if (query != null && !query.isBlank()) {
            sb.append("    ")
              .append("Object ")
              .append(query)
              .append("(Object value);\n\n");
        }

        sb.append("}\n");

        return sb.toString();
    }
}