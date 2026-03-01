package io.github.mdasifmustafa.sbx.template.controller;

public final class RestControllerTemplate {

    private RestControllerTemplate() {}

    public static String generate(String pkg, String className, String path) {
        String service = className.replace("Controller", "Service");
        String servicePkg = pkg.replace(".controller", ".service");

        return ""
            + "package " + pkg + ";\n\n"
            + "import org.springframework.web.bind.annotation.*;\n"
            + "import " + servicePkg + "." + service + ";\n\n"
            + "@RestController\n"
            + "@RequestMapping(\"" + path + "\")\n"
            + "public class " + className + " {\n\n"
            + "    private final " + service + " service;\n\n"
            + "    public " + className + "(" + service + " service) {\n"
            + "        this.service = service;\n"
            + "    }\n\n"
            + "    @GetMapping\n"
            + "    public String index() {\n"
            + "        return service.toString();\n"
            + "    }\n"
            + "}\n";
    }
}