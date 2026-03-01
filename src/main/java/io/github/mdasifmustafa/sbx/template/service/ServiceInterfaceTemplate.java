package io.github.mdasifmustafa.sbx.template.service;

public final class ServiceInterfaceTemplate {

    private ServiceInterfaceTemplate() {
    }

    public static String generate(String pkg, String name) {
        return ""
            + "package " + pkg + ";\n\n"
            + "public interface " + name + "Service {\n"
            + "}\n";
    }
}