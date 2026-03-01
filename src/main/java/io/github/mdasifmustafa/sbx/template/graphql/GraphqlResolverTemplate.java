package io.github.mdasifmustafa.sbx.template.graphql;

public final class GraphqlResolverTemplate {

    private GraphqlResolverTemplate() {}

    public static String generate(
            String pkg,
            String name,
            boolean query,
            boolean mutation
    ) {
        String service = name + "Service";
        String servicePkg = pkg.replace(".api.graphql", ".service");

        StringBuilder sb = new StringBuilder();

        sb.append("package ").append(pkg).append(";\n\n");
        sb.append("import org.springframework.stereotype.Controller;\n");
        sb.append("import ").append(servicePkg).append(".").append(service).append(";\n");

        if (query) {
            sb.append("import org.springframework.graphql.data.method.annotation.QueryMapping;\n");
        }
        if (mutation) {
            sb.append("import org.springframework.graphql.data.method.annotation.MutationMapping;\n");
        }

        sb.append("\n@Controller\n");
        sb.append("public class ").append(name).append("Resolver {\n\n");

        sb.append("    private final ").append(service).append(" service;\n\n");
        sb.append("    public ").append(name).append("Resolver(")
          .append(service).append(" service) {\n");
        sb.append("        this.service = service;\n");
        sb.append("    }\n\n");

        if (query) {
            sb.append("    @QueryMapping\n");
            sb.append("    public String ").append(name.toLowerCase()).append("() {\n");
            sb.append("        return service.toString();\n");
            sb.append("    }\n\n");
        }

        if (mutation) {
            sb.append("    @MutationMapping\n");
            sb.append("    public String create").append(name).append("() {\n");
            sb.append("        return service.toString();\n");
            sb.append("    }\n\n");
        }

        sb.append("}\n");

        return sb.toString();
    }
}