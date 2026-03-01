package io.github.mdasifmustafa.sbx.template.mapper;

public final class EntityMapperTemplate {

    private EntityMapperTemplate() {}

    public static String generate(String pkg, String entity) {
        String basePkg = pkg.replace(".api.mapper", "");

        return ""
            + "package " + pkg + ";\n\n"
            + "import org.mapstruct.Mapper;\n"
            + "import org.mapstruct.MappingTarget;\n"
            + "import " + basePkg + ".domain." + entity.toLowerCase() + "." + entity + ";\n"
            + "import " + basePkg + ".api.dto." + entity + "RequestDto;\n"
            + "import " + basePkg + ".api.dto." + entity + "ResponseDto;\n\n"
            + "@Mapper(componentModel = \"spring\")\n"
            + "public interface " + entity + "Mapper {\n\n"
            + "    " + entity + "ResponseDto toDto(" + entity + " entity);\n\n"
            + "    " + entity + " toEntity(" + entity + "RequestDto dto);\n\n"
            + "    void update(@MappingTarget " + entity + " entity, "
            + entity + "RequestDto dto);\n"
            + "}\n";
    }
}