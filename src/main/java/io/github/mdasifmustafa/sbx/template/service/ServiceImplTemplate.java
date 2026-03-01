package io.github.mdasifmustafa.sbx.template.service;

public final class ServiceImplTemplate {

    private ServiceImplTemplate() {
    }

    public static String generate(String pkg, String name) {
        return ""
            + "package " + pkg + ";\n\n"
            + "import org.springframework.stereotype.Service;\n"
            + "import org.springframework.transaction.annotation.Transactional;\n\n"
            + "@Service\n"
            + "@Transactional\n"
            + "public class " + name + "ServiceImpl implements " + name + "Service {\n"
            + "}\n";
    }
}