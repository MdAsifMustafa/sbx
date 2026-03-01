package io.github.mdasifmustafa.sbx.template;

import io.github.mdasifmustafa.sbx.command.make.ControllerType;

public final class Templates {

    private Templates() {
    }

    // --------------------------------------------------
    // CONTROLLER
    // --------------------------------------------------
    public static String controller(
            String pkg,
            String className,
            ControllerType type,
            String model,
            String path,
            boolean versioned,
            boolean service
    ) {

        String mapping = (versioned ? "/api/v1" : "") + path;

        if (type == ControllerType.GRAPHQL) {
            return graphqlController(pkg, className);
        }

        if (type == ControllerType.CRUD) {
            return crudController(pkg, className, mapping);
        }

        if (type == ControllerType.REST) {
            return restController(pkg, className, mapping);
        }

        return mvcController(pkg, className);
    }

    // --------------------------------------------------
    // MVC
    // --------------------------------------------------
    private static String mvcController(String pkg, String className) {
        return ""
                + "package " + pkg + ";\n\n"
                + "import org.springframework.stereotype.Controller;\n\n"
                + "@Controller\n"
                + "public class " + className + " {\n"
                + "}\n";
    }

    // --------------------------------------------------
    // REST
    // --------------------------------------------------
    private static String restController(String pkg, String className, String path) {
        return ""
                + "package " + pkg + ";\n\n"
                + "import org.springframework.web.bind.annotation.GetMapping;\n"
                + "import org.springframework.web.bind.annotation.RequestMapping;\n"
                + "import org.springframework.web.bind.annotation.RestController;\n\n"
                + "@RestController\n"
                + "@RequestMapping(\"" + path + "\")\n"
                + "public class " + className + " {\n\n"
                + "    @GetMapping\n"
                + "    public String index() {\n"
                + "        return \"hello\";\n"
                + "    }\n"
                + "}\n";
    }

    // --------------------------------------------------
    // CRUD
    // --------------------------------------------------
    private static String crudController(String pkg, String className, String path) {
        return ""
                + "package " + pkg + ";\n\n"
                + "import org.springframework.web.bind.annotation.*;\n\n"
                + "@RestController\n"
                + "@RequestMapping(\"" + path + "\")\n"
                + "public class " + className + " {\n\n"
                + "    @GetMapping\n"
                + "    public String index() {\n"
                + "        return \"index\";\n"
                + "    }\n\n"
                + "    @PostMapping\n"
                + "    public String store() {\n"
                + "        return \"store\";\n"
                + "    }\n\n"
                + "    @GetMapping(\"/{id}\")\n"
                + "    public String show(@PathVariable Long id) {\n"
                + "        return \"show\";\n"
                + "    }\n\n"
                + "    @PutMapping(\"/{id}\")\n"
                + "    public String update(@PathVariable Long id) {\n"
                + "        return \"update\";\n"
                + "    }\n\n"
                + "    @DeleteMapping(\"/{id}\")\n"
                + "    public String delete(@PathVariable Long id) {\n"
                + "        return \"delete\";\n"
                + "    }\n"
                + "}\n";
    }

    // --------------------------------------------------
    // GRAPHQL
    // --------------------------------------------------
    private static String graphqlController(String pkg, String className) {
        return ""
                + "package " + pkg + ";\n\n"
                + "import org.springframework.graphql.data.method.annotation.QueryMapping;\n"
                + "import org.springframework.stereotype.Controller;\n\n"
                + "@Controller\n"
                + "public class " + className + " {\n\n"
                + "    @QueryMapping\n"
                + "    public String hello() {\n"
                + "        return \"Hello GraphQL\";\n"
                + "    }\n"
                + "}\n";
    }

    // --------------------------------------------------
    // SERVICE
    // --------------------------------------------------
    public static String service(String pkg, String className, String model) {
        return ""
                + "package " + pkg + ";\n\n"
                + "import org.springframework.stereotype.Service;\n\n"
                + "@Service\n"
                + "public class " + className + " {\n"
                + "}\n";
    }

    // --------------------------------------------------
    // TEST
    // --------------------------------------------------
    public static String controllerTest(String pkg, String className) {
        return ""
                + "package " + pkg + ";\n\n"
                + "import org.junit.jupiter.api.Test;\n\n"
                + "class " + className + "Test {\n\n"
                + "    @Test\n"
                + "    void contextLoads() {\n"
                + "    }\n"
                + "}\n";
    }
    
    public static String entity(String pkg, String name) {
        return ""
            + "package " + pkg + ";\n\n"
            + "import jakarta.persistence.*;\n\n"
            + "@Entity\n"
            + "public class " + name + " {\n\n"
            + "    @Id\n"
            + "    @GeneratedValue(strategy = GenerationType.IDENTITY)\n"
            + "    private Long id;\n\n"
            + "}\n";
    }
    
    public static String repository(String pkg, String entity) {
        return ""
            + "package " + pkg + ";\n\n"
            + "import org.springframework.data.jpa.repository.JpaRepository;\n\n"
            + "public interface " + entity + "Repository "
            + "extends JpaRepository<" + entity + ", Long> {\n"
            + "}\n";
    }
}