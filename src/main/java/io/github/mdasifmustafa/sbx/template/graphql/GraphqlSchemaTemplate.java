package io.github.mdasifmustafa.sbx.template.graphql;

public final class GraphqlSchemaTemplate {

    private GraphqlSchemaTemplate() {}

    public static String generate(
            String name,
            boolean query,
            boolean mutation
    ) {
        StringBuilder sb = new StringBuilder();

        if (query) {
            sb.append("type Query {\n");
            sb.append("  ").append(name.toLowerCase()).append(": String\n");
            sb.append("}\n\n");
        }

        if (mutation) {
            sb.append("type Mutation {\n");
            sb.append("  create").append(name).append(": String\n");
            sb.append("}\n\n");
        }

        sb.append("type ").append(name).append(" {\n");
        sb.append("  id: ID\n");
        sb.append("}\n");

        return sb.toString();
    }
}