package io.github.mdasifmustafa.sbx.template.event;

public final class EventTemplate {

    private EventTemplate() {
    }

    public static String generateEvent(String pkg, String className, boolean payload) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(";\n\n");
        sb.append("import java.time.Instant;\n\n");
        sb.append("public class ").append(className).append(" {\n\n");
        sb.append("    private final Instant occurredAt;\n");
        if (payload) {
            sb.append("    private final Object payload;\n\n");
            sb.append("    public ").append(className).append("(Object payload) {\n");
            sb.append("        this.payload = payload;\n");
            sb.append("        this.occurredAt = Instant.now();\n");
            sb.append("    }\n\n");
            sb.append("    public Object getPayload() {\n");
            sb.append("        return payload;\n");
            sb.append("    }\n\n");
        } else {
            sb.append("\n    public ").append(className).append("() {\n");
            sb.append("        this.occurredAt = Instant.now();\n");
            sb.append("    }\n\n");
        }
        sb.append("    public Instant getOccurredAt() {\n");
        sb.append("        return occurredAt;\n");
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }

    public static String generateListener(
            String listenerPkg,
            String eventPkg,
            String eventClass,
            String listenerClass) {
        return ""
                + "package " + listenerPkg + ";\n\n"
                + "import " + eventPkg + "." + eventClass + ";\n"
                + "import org.springframework.context.event.EventListener;\n"
                + "import org.springframework.stereotype.Component;\n\n"
                + "@Component\n"
                + "public class " + listenerClass + " {\n\n"
                + "    @EventListener\n"
                + "    public void on" + eventClass + "(" + eventClass + " event) {\n"
                + "        // TODO: handle event\n"
                + "    }\n"
                + "}\n";
    }
}
