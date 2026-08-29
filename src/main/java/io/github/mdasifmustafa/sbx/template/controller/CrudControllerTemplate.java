package io.github.mdasifmustafa.sbx.template.controller;

public final class CrudControllerTemplate {

    private CrudControllerTemplate() {}

    public static String generate(String pkg, String name, String path, boolean validation) {
        String base = pkg;
        int controllerIndex = pkg.lastIndexOf(".controller");
        if (controllerIndex > -1) {
            base = pkg.substring(0, controllerIndex);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(";\n\n");
        sb.append("import java.util.List;\n");
        sb.append("import org.springframework.data.domain.Page;\n");
        sb.append("import org.springframework.data.domain.Pageable;\n");
        sb.append("import org.springframework.http.ResponseEntity;\n");
        sb.append("import org.springframework.web.bind.annotation.*;\n");
        sb.append("import ").append(base).append(".api.dto.").append(name).append("RequestDto;\n");
        sb.append("import ").append(base).append(".api.dto.").append(name).append("ResponseDto;\n");
        sb.append("import ").append(base).append(".service.").append(name).append("Service;\n\n");

        if (validation) {
            sb.append("import org.springframework.validation.annotation.Validated;\n\n");
        }

        sb.append("@RestController\n");
        if (validation) {
            sb.append("@Validated\n");
        }
        sb.append("@RequestMapping(\"").append(path).append("\")\n");
        sb.append("public class ").append(name).append("Controller {\n\n");
        sb.append("    private final ").append(name).append("Service service;\n\n");
        sb.append("    public ").append(name).append("Controller(").append(name).append("Service service) {\n");
        sb.append("        this.service = service;\n");
        sb.append("    }\n\n");

        sb.append("    @PostMapping\n");
        sb.append("    public ResponseEntity<").append(name).append("ResponseDto> create(@Validated @RequestBody ").append(name).append("RequestDto dto) {\n");
        sb.append("        return ResponseEntity.ok(service.create(dto));\n");
        sb.append("    }\n\n");

        sb.append("    @PutMapping(\"/{id}\")\n");
        sb.append("    public ResponseEntity<").append(name).append("ResponseDto> update(\n");
        sb.append("            @PathVariable Long id,\n");
        sb.append("            @Validated @RequestBody ").append(name).append("RequestDto dto) {\n");
        sb.append("        return ResponseEntity.ok(service.update(id, dto));\n");
        sb.append("    }\n\n");

        sb.append("    @GetMapping(\"/{id}\")\n");
        sb.append("    public ResponseEntity<").append(name).append("ResponseDto> get(@PathVariable Long id) {\n");
        sb.append("        return ResponseEntity.ok(service.getById(id));\n");
        sb.append("    }\n\n");

        sb.append("    @DeleteMapping(\"/{id}\")\n");
        sb.append("    public ResponseEntity<Void> delete(@PathVariable Long id) {\n");
        sb.append("        service.delete(id);\n");
        sb.append("        return ResponseEntity.noContent().build();\n");
        sb.append("    }\n\n");

        sb.append("    @GetMapping(\"/all\")\n");
        sb.append("    public ResponseEntity<List<").append(name).append("ResponseDto>> list() {\n");
        sb.append("        return ResponseEntity.ok(service.getAll());\n");
        sb.append("    }\n\n");

        sb.append("    @GetMapping\n");
        sb.append("    public ResponseEntity<Page<").append(name).append("ResponseDto>> page(Pageable pageable) {\n");
        sb.append("        return ResponseEntity.ok(service.getPage(pageable));\n");
        sb.append("    }\n\n");

        sb.append("}\n");

        return sb.toString();
    }
}