package io.github.mdasifmustafa.sbx.template.mail;

public final class MailTemplate {

    private MailTemplate() {
    }

    public static String generateService(String pkg, String className, boolean async) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(";\n\n");
        if (async) {
            sb.append("import org.springframework.scheduling.annotation.Async;\n");
        }
        sb.append("import org.springframework.stereotype.Service;\n\n");
        sb.append("@Service\n");
        sb.append("public class ").append(className).append(" {\n\n");
        if (async) {
            sb.append("    @Async\n");
        }
        sb.append("    public void send(String to, String subject, String body) {\n");
        sb.append("        // TODO: implement mail provider integration\n");
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }

    public static String generateHtmlTemplate(String name) {
        return ""
                + "<!DOCTYPE html>\n"
                + "<html lang=\"en\">\n"
                + "<head>\n"
                + "  <meta charset=\"UTF-8\" />\n"
                + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n"
                + "  <title>" + name + " Mail</title>\n"
                + "</head>\n"
                + "<body>\n"
                + "  <h2>" + name + "</h2>\n"
                + "  <p>Hello {{name}},</p>\n"
                + "  <p>{{message}}</p>\n"
                + "</body>\n"
                + "</html>\n";
    }
}
