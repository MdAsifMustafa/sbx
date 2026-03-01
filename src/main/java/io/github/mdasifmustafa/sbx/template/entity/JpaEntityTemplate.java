package io.github.mdasifmustafa.sbx.template.entity;

public final class JpaEntityTemplate {

    private JpaEntityTemplate() {}

    public static String generate(
            String pkg,
            String name,
            String table,
            boolean uuid,
            boolean auditable,
            boolean softDelete,
            boolean lombok
    ) {
        StringBuilder sb = new StringBuilder();

        sb.append("package ").append(pkg).append(";\n\n");
        sb.append("import jakarta.persistence.*;\n");

        if (uuid) {
            sb.append("import java.util.UUID;\n");
        }
        if (auditable || softDelete) {
            sb.append("import java.time.LocalDateTime;\n");
            sb.append("import org.springframework.data.annotation.CreatedDate;\n");
            sb.append("import org.springframework.data.annotation.LastModifiedDate;\n");
            sb.append("import org.springframework.data.jpa.domain.support.AuditingEntityListener;\n");
        }
        
        if (lombok) {
            sb.append("import lombok.Getter;\n");
            sb.append("import lombok.Setter;\n");
            sb.append("import lombok.NoArgsConstructor;\n");
        }

        sb.append("\n");
        
        if (lombok) {
            sb.append("@Getter\n");
            sb.append("@Setter\n");
            sb.append("@NoArgsConstructor\n");
        }
        sb.append("@Entity\n");
        if(auditable) {
        	sb.append("@EntityListeners(AuditingEntityListener.class)\n");
        }
        if (table != null) {
            sb.append("@Table(name = \"").append(table).append("\")\n");
        }

        sb.append("public class ").append(name).append(" {\n\n");

        sb.append("    @Id\n");
        if (uuid) {
            sb.append("    @GeneratedValue\n");
            sb.append("    private UUID id;\n\n");
        } else {
            sb.append("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n");
            sb.append("    private Long id;\n\n");
        }

        if (auditable) {
            sb.append("    private String createdBy;\n\n");
            sb.append("    private String updatedBy;\n\n");
            sb.append("    @CreatedDate\r\n");
            sb.append("    private LocalDateTime createdAt;\n\n");
            sb.append("    @LastModifiedDate\r\n");
            sb.append("    private LocalDateTime updatedAt;\n\n");
        }

        if (softDelete) {
            sb.append("    private LocalDateTime deletedAt;\n\n");
        }

        sb.append("}\n");

        return sb.toString();
    }
}