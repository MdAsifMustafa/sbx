package io.github.mdasifmustafa.sbx.template;

import io.github.mdasifmustafa.sbx.command.make.ControllerType;
import io.github.mdasifmustafa.sbx.template.controller.CrudControllerTemplate;
import io.github.mdasifmustafa.sbx.template.controller.MvcControllerTemplate;
import io.github.mdasifmustafa.sbx.template.controller.RestControllerTemplate;
import io.github.mdasifmustafa.sbx.template.dto.DtoTemplate;
import io.github.mdasifmustafa.sbx.template.dto.EntityDtoTemplate;
import io.github.mdasifmustafa.sbx.template.entity.JpaEntityTemplate;
import io.github.mdasifmustafa.sbx.template.event.EventTemplate;
import io.github.mdasifmustafa.sbx.template.graphql.GraphqlResolverTemplate;
import io.github.mdasifmustafa.sbx.template.graphql.GraphqlSchemaTemplate;
import io.github.mdasifmustafa.sbx.template.mail.MailTemplate;
import io.github.mdasifmustafa.sbx.template.migration.FlywayMigrationTemplate;
import io.github.mdasifmustafa.sbx.template.repository.JpaRepositoryTemplate;
import io.github.mdasifmustafa.sbx.template.service.CrudServiceTemplate;
import io.github.mdasifmustafa.sbx.template.service.ServiceImplTemplate;
import io.github.mdasifmustafa.sbx.template.service.ServiceInterfaceTemplate;

public final class TemplateEngine {

    private TemplateEngine() {
    }

    // --------------------------------------------------
    // CONTROLLER
    // --------------------------------------------------
    public static String controller(
            String pkg,
            String className,
            ControllerType type,
            String path
    ) {
        switch (type) {
            case GRAPHQL:
                return GraphqlResolverTemplate.generate(pkg, className, true, true);
            case REST:
                return RestControllerTemplate.generate(pkg, className, path);
            case CRUD:
                return CrudControllerTemplate.generate(pkg, className, path);
            default:
                return MvcControllerTemplate.generate(pkg, className);
        }
    }

    // --------------------------------------------------
    // SERVICE
    // --------------------------------------------------
    public static String serviceInterface(String pkg, String name) {
        return ServiceInterfaceTemplate.generate(pkg, name);
    }

    public static String serviceImpl(String pkg, String name) {
        return ServiceImplTemplate.generate(pkg, name);
    }
    
    public static String crudServiceInterface(String pkg, String name) {
        return CrudServiceTemplate.serviceInterface(pkg, name);
    }

    public static String crudServiceImpl(String pkg, String name) {
        return CrudServiceTemplate.serviceImpl(pkg, name);
    }

    public static String crudController(String pkg, String name, String path) {
        return CrudControllerTemplate.generate(pkg, name, path);
    }

    // --------------------------------------------------
    // REPOSITORY
    // --------------------------------------------------
    public static String repository(
            String pkg,
            String entity,
            boolean custom,
            String query
    ) {
        return JpaRepositoryTemplate.generate(pkg, entity, custom, query);
    }

    // --------------------------------------------------
    // Entity
    // --------------------------------------------------    
    public static String entity(
            String pkg,
            String name,
            String table,
            boolean uuid,
            boolean auditable,
            boolean softDelete,
            boolean lombok
    ) {
        return JpaEntityTemplate.generate(pkg, name, table, uuid, auditable, softDelete,lombok);
    }

    // --------------------------------------------------
    // Dto
    // --------------------------------------------------  
    public static String dto(
            String pkg,
            String name,
            boolean record,
            boolean validation
    ) {
        return DtoTemplate.generate(pkg, name, record, validation);
    }
    public static String dtoFromEntity(
            String pkg,
            String dtoName,
            String entityName,
            boolean request,
            boolean response,
            boolean record,
            boolean lombok
    ) {
        return EntityDtoTemplate.generate(
            pkg,
            dtoName,
            entityName,
            request,
            response,
            record,
            lombok
        );
    }
    

    // --------------------------------------------------
    // Flyway Migration
    // --------------------------------------------------  
    
    public static String migration(String name) {
        return FlywayMigrationTemplate.generate(name);
    }
    


    // --------------------------------------------------
    // Graphql
    // --------------------------------------------------  
    
    public static String graphqlResolver(
            String pkg,
            String name,
            boolean query,
            boolean mutation
    ) {
        return GraphqlResolverTemplate.generate(pkg, name, query, mutation);
    }

    public static String graphqlSchema(
            String name,
            boolean query,
            boolean mutation
    ) {
        return GraphqlSchemaTemplate.generate(name, query, mutation);
    }

    // --------------------------------------------------
    // Event
    // --------------------------------------------------
    public static String event(String pkg, String className, boolean payload) {
        return EventTemplate.generateEvent(pkg, className, payload);
    }

    public static String eventListener(
            String listenerPkg,
            String eventPkg,
            String eventClass,
            String listenerClass) {
        return EventTemplate.generateListener(listenerPkg, eventPkg, eventClass, listenerClass);
    }

    // --------------------------------------------------
    // Mail
    // --------------------------------------------------
    public static String mailService(String pkg, String className, boolean async) {
        return MailTemplate.generateService(pkg, className, async);
    }

    public static String mailTemplate(String name) {
        return MailTemplate.generateHtmlTemplate(name);
    }

    // --------------------------------------------------
    // Scheduler
    // --------------------------------------------------
    public static String scheduler(String pkg, String className, String scheduleExpr, String scheduleType, boolean async) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(";\n\n");
        if (async) {
            sb.append("import org.springframework.scheduling.annotation.Async;\n");
        }
        sb.append("import org.springframework.scheduling.annotation.Scheduled;\n");
        sb.append("import org.springframework.stereotype.Component;\n\n");
        sb.append("@Component\n");
        sb.append("public class ").append(className).append(" {\n\n");
        if (async) {
            sb.append("    @Async\n");
        }
        if ("fixedRate".equals(scheduleType)) {
            sb.append("    @Scheduled(fixedRateString = \"").append(scheduleExpr).append("\")\n");
        } else if ("fixedDelay".equals(scheduleType)) {
            sb.append("    @Scheduled(fixedDelayString = \"").append(scheduleExpr).append("\")\n");
        } else {
            sb.append("    @Scheduled(cron = \"").append(scheduleExpr).append("\")\n");
        }
        sb.append("    public void run() {\n");
        sb.append("        // TODO: implement job logic\n");
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }

    // --------------------------------------------------
    // Exception
    // --------------------------------------------------
    public static String exception(String pkg, String className, String code) {
        return ""
                + "package " + pkg + ";\n\n"
                + "public class " + className + " extends RuntimeException {\n"
                + "    public static final String CODE = \"" + code + "\";\n\n"
                + "    public " + className + "(String message) {\n"
                + "        super(message);\n"
                + "    }\n"
                + "}\n";
    }

    public static String exceptionHandler(String pkg, String exPkg, String exClass, int status) {
        return ""
                + "package " + pkg + ";\n\n"
                + "import " + exPkg + "." + exClass + ";\n"
                + "import org.springframework.http.HttpStatus;\n"
                + "import org.springframework.http.ResponseEntity;\n"
                + "import org.springframework.web.bind.annotation.ExceptionHandler;\n"
                + "import org.springframework.web.bind.annotation.RestControllerAdvice;\n\n"
                + "@RestControllerAdvice\n"
                + "public class GlobalExceptionHandler {\n\n"
                + "    @ExceptionHandler(" + exClass + ".class)\n"
                + "    public ResponseEntity<String> handle(" + exClass + " ex) {\n"
                + "        return ResponseEntity.status(HttpStatus.valueOf(" + status + ")).body(ex.getMessage());\n"
                + "    }\n"
                + "}\n";
    }

    // --------------------------------------------------
    // Validator
    // --------------------------------------------------
    public static String validatorAnnotation(String pkg, String name, String message) {
        return ""
                + "package " + pkg + ";\n\n"
                + "import jakarta.validation.Constraint;\n"
                + "import jakarta.validation.Payload;\n"
                + "import java.lang.annotation.*;\n\n"
                + "@Documented\n"
                + "@Constraint(validatedBy = " + name + "Validator.class)\n"
                + "@Target({ ElementType.FIELD, ElementType.PARAMETER })\n"
                + "@Retention(RetentionPolicy.RUNTIME)\n"
                + "public @interface " + name + " {\n"
                + "    String message() default \"" + message + "\";\n"
                + "    Class<?>[] groups() default {};\n"
                + "    Class<? extends Payload>[] payload() default {};\n"
                + "}\n";
    }

    public static String validatorClass(String pkg, String name, String fieldType) {
        return ""
                + "package " + pkg + ";\n\n"
                + "import jakarta.validation.ConstraintValidator;\n"
                + "import jakarta.validation.ConstraintValidatorContext;\n\n"
                + "public class " + name + "Validator implements ConstraintValidator<" + name + ", " + fieldType + "> {\n"
                + "    @Override\n"
                + "    public boolean isValid(" + fieldType + " value, ConstraintValidatorContext context) {\n"
                + "        if (value == null) return true;\n"
                + "        // TODO: implement validation rule\n"
                + "        return true;\n"
                + "    }\n"
                + "}\n";
    }

    // --------------------------------------------------
    // Specification
    // --------------------------------------------------
    public static String specification(String pkg, String className, String entityName, boolean paging, boolean sorting) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(";\n\n");
        sb.append("import org.springframework.data.jpa.domain.Specification;\n");
        if (paging || sorting) {
            sb.append("import org.springframework.data.domain.PageRequest;\n");
            sb.append("import org.springframework.data.domain.Sort;\n");
        }
        sb.append("\npublic final class ").append(className).append(" {\n\n");
        sb.append("    private ").append(className).append("() {}\n\n");
        sb.append("    public static Specification<").append(entityName).append("> byKeyword(String keyword) {\n");
        sb.append("        return (root, query, cb) -> cb.conjunction();\n");
        sb.append("    }\n");
        if (paging || sorting) {
            sb.append("\n    public static PageRequest pageRequest(int page, int size) {\n");
            if (sorting) {
                sb.append("        return PageRequest.of(page, size, Sort.by(\"id\").descending());\n");
            } else {
                sb.append("        return PageRequest.of(page, size);\n");
            }
            sb.append("    }\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    // --------------------------------------------------
    // Security
    // --------------------------------------------------
    public static String securityConfig(String pkg, String className, boolean methodSecurity, boolean jwt, boolean roles) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(";\n\n");
        sb.append("import org.springframework.context.annotation.Bean;\n");
        sb.append("import org.springframework.context.annotation.Configuration;\n");
        sb.append("import org.springframework.security.config.annotation.web.builders.HttpSecurity;\n");
        if (methodSecurity) {
            sb.append("import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;\n");
        }
        sb.append("import org.springframework.security.web.SecurityFilterChain;\n\n");
        sb.append("@Configuration\n");
        if (methodSecurity) {
            sb.append("@EnableMethodSecurity\n");
        }
        sb.append("public class ").append(className).append(" {\n\n");
        sb.append("    @Bean\n");
        sb.append("    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {\n");
        sb.append("        http.csrf(csrf -> csrf.disable())\n");
        sb.append("           .authorizeHttpRequests(auth -> auth\n");
        if (roles) {
            sb.append("               .requestMatchers(\"/admin/**\").hasRole(\"ADMIN\")\n");
        }
        sb.append("               .anyRequest().authenticated());\n");
        if (jwt) {
            sb.append("        // TODO: configure JWT resource server/authentication provider\n");
        }
        sb.append("        return http.build();\n");
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }

    // --------------------------------------------------
    // Cache
    // --------------------------------------------------
    public static String cacheService(String pkg, String className, String provider, String ttl, String keyPrefix) {
        return ""
                + "package " + pkg + ";\n\n"
                + "import org.springframework.cache.annotation.Cacheable;\n"
                + "import org.springframework.stereotype.Service;\n\n"
                + "@Service\n"
                + "public class " + className + " {\n\n"
                + "    // provider=" + provider + ", ttl=" + ttl + ", keyPrefix=" + keyPrefix + "\n"
                + "    @Cacheable(value = \"" + keyPrefix + "\", key = \"#key\")\n"
                + "    public Object getOrLoad(String key) {\n"
                + "        // TODO: load and return value\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n";
    }

    // --------------------------------------------------
    // Message
    // --------------------------------------------------
    public static String messageProducer(String pkg, String className, String topic) {
        return ""
                + "package " + pkg + ";\n\n"
                + "import org.springframework.stereotype.Component;\n\n"
                + "@Component\n"
                + "public class " + className + "Producer {\n\n"
                + "    private static final String TOPIC = \"" + topic + "\";\n\n"
                + "    public void send(" + className + "Payload payload) {\n"
                + "        // TODO: publish payload to topic/queue\n"
                + "    }\n"
                + "}\n";
    }

    public static String messageConsumer(String pkg, String className, String topic, String group) {
        return ""
                + "package " + pkg + ";\n\n"
                + "import org.springframework.stereotype.Component;\n\n"
                + "@Component\n"
                + "public class " + className + "Consumer {\n\n"
                + "    // topic=" + topic + ", group=" + group + "\n"
                + "    public void onMessage(" + className + "Payload payload) {\n"
                + "        // TODO: consume payload from topic/queue\n"
                + "    }\n"
                + "}\n";
    }

    public static String messagePayload(String pkg, String className) {
        return ""
                + "package " + pkg + ";\n\n"
                + "public class " + className + "Payload {\n"
                + "    private String id;\n"
                + "    private String value;\n\n"
                + "    public String getId() {\n"
                + "        return id;\n"
                + "    }\n\n"
                + "    public void setId(String id) {\n"
                + "        this.id = id;\n"
                + "    }\n\n"
                + "    public String getValue() {\n"
                + "        return value;\n"
                + "    }\n\n"
                + "    public void setValue(String value) {\n"
                + "        this.value = value;\n"
                + "    }\n"
                + "}\n";
    }

    // --------------------------------------------------
    // MapStruct
    // --------------------------------------------------
    public static String mapstructMapper(String pkg, String entity, String dto, String componentModel, boolean updateMethod) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(";\n\n");
        sb.append("import org.mapstruct.Mapper;\n");
        if (updateMethod) {
            sb.append("import org.mapstruct.MappingTarget;\n");
        }
        sb.append("\n@Mapper(componentModel = \"").append(componentModel).append("\")\n");
        sb.append("public interface ").append(entity).append("MapStructMapper {\n\n");
        sb.append("    ").append(dto).append(" toDto(").append(entity).append(" entity);\n");
        sb.append("    ").append(entity).append(" toEntity(").append(dto).append(" dto);\n");
        if (updateMethod) {
            sb.append("    void updateEntity(").append(dto).append(" dto, @MappingTarget ").append(entity).append(" entity);\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    // --------------------------------------------------
    // Test
    // --------------------------------------------------
    public static String testClass(String pkg, String className, String type, boolean mockito) {
        String importLine = mockito ? "import static org.mockito.Mockito.*;\n" : "";
        return ""
                + "package " + pkg + ";\n\n"
                + "import org.junit.jupiter.api.Test;\n"
                + importLine
                + "import static org.junit.jupiter.api.Assertions.*;\n\n"
                + "class " + className + " {\n\n"
                + "    @Test\n"
                + "    void shouldRun" + type + "Test() {\n"
                + "        assertTrue(true);\n"
                + "    }\n"
                + "}\n";
    }

    // --------------------------------------------------
    // Module
    // --------------------------------------------------
    public static String moduleReadme(String moduleName, boolean withCrud, boolean withEvent, boolean withMail) {
        return ""
                + "# " + moduleName + " Module\n\n"
                + "Generated by SBX make module.\n\n"
                + "- CRUD scaffolding: " + withCrud + "\n"
                + "- Event scaffolding: " + withEvent + "\n"
                + "- Mail scaffolding: " + withMail + "\n";
    }
}
