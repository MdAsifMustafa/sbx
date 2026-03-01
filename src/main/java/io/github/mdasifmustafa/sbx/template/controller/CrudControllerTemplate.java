package io.github.mdasifmustafa.sbx.template.controller;

public final class CrudControllerTemplate {

    private CrudControllerTemplate() {}

    public static String generate(String pkg, String name, String path) {
        String base = pkg.substring(0, pkg.lastIndexOf(".controller"));

        return ""
            + "package " + pkg + ";\n\n"
            + "import java.util.List;\n"
            + "import jakarta.validation.Valid;\n"
            + "import org.springframework.data.domain.Page;\n"
            + "import org.springframework.data.domain.Pageable;\n"
            + "import org.springframework.http.ResponseEntity;\n"
            + "import org.springframework.web.bind.annotation.*;\n"
            + "import " + base + ".api.dto." + name + "RequestDto;\n"
            + "import " + base + ".api.dto." + name + "ResponseDto;\n"
            + "import " + base + ".service." + name + "Service;\n\n"
            + "@RestController\n"
            + "@RequestMapping(\"" + path + "\")\n"
            + "public class " + name + "Controller {\n\n"
            + "    private final " + name + "Service service;\n\n"
            + "    public " + name + "Controller(" + name + "Service service) {\n"
            + "        this.service = service;\n"
            + "    }\n\n"
            + "    @PostMapping\n"
            + "    public ResponseEntity<" + name + "ResponseDto> create(@Valid @RequestBody " + name + "RequestDto dto) {\n"
            + "        return ResponseEntity.ok(service.create(dto));\n"
            + "    }\n\n"
            + "    @PutMapping(\"/{id}\")\n"
            + "    public ResponseEntity<" + name + "ResponseDto> update(\n"
            + "            @PathVariable Long id,\n"
            + "            @Valid @RequestBody " + name + "RequestDto dto) {\n"
            + "        return ResponseEntity.ok(service.update(id, dto));\n"
            + "    }\n\n"
            + "    @GetMapping(\"/{id}\")\n"
            + "    public ResponseEntity<" + name + "ResponseDto> get(@PathVariable Long id) {\n"
            + "        return ResponseEntity.ok(service.getById(id));\n"
            + "    }\n\n"
            + "    @DeleteMapping(\"/{id}\")\n"
            + "    public ResponseEntity<Void> delete(@PathVariable Long id) {\n"
            + "        service.delete(id);\n"
            + "        return ResponseEntity.noContent().build();\n"
            + "    }\n\n"
            + "    @GetMapping(\"/all\")\n"
            + "    public ResponseEntity<List<" + name + "ResponseDto>> list() {\n"
            + "        return ResponseEntity.ok(service.getAll());\n"
            + "    }\n\n"
            + "    @GetMapping\n"
            + "    public ResponseEntity<Page<" + name + "ResponseDto>> page(Pageable pageable) {\n"
            + "        return ResponseEntity.ok(service.getPage(pageable));\n"
            + "    }\n\n"
            + "}\n";
    }
}