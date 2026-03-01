package io.github.mdasifmustafa.sbx.dependency;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mdasifmustafa.sbx.config.DependencyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class DependencyRuleRegistry {

    private static final Logger logger = LoggerFactory.getLogger(DependencyRuleRegistry.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static JsonNode ROOT;

    static {
        try (InputStream is =
                     DependencyRuleRegistry.class
                             .getClassLoader()
                             .getResourceAsStream("dependency-rules.json")) {

            if (is != null) {
                ROOT = MAPPER.readTree(is);
            } else {
                ROOT = MAPPER.createObjectNode();
            }

        } catch (IOException e) {
            logger.warn("Failed to load dependency-rules.json", e);
            ROOT = MAPPER.createObjectNode();
        }
    }

    private DependencyRuleRegistry() {}

    // ======================================================
    // APPLY RULES
    // ======================================================

    public static void applyRules(String groupId,
                                  String artifactId,
                                  DependencyConfig dep) {

        String a = artifactId.toLowerCase();
        String g = groupId.toLowerCase();

        applyScopeRules(a, g, dep);
        applyAnnotationProcessorRules(a, dep);
        applyOptionalRules(a, dep);
    }

    // ======================================================
    // SCOPES
    // ======================================================

    private static void applyScopeRules(String artifactId,
                                        String groupId,
                                        DependencyConfig dep) {

        JsonNode scopes = ROOT.get("scopes");

        if (scopes != null) {
            Iterator<String> names = scopes.fieldNames();

            while (names.hasNext()) {
                String scope = names.next();

                for (JsonNode rule : scopes.get(scope)) {
                    String keyword = rule.asText().toLowerCase();

                    if (artifactId.contains(keyword)) {
                        dep.scope = scope;
                        return;
                    }
                }
            }
        }

        // groupScopes fallback
        JsonNode groupScopes = ROOT.get("groupScopes");

        if (groupScopes != null) {
            Iterator<String> names = groupScopes.fieldNames();

            while (names.hasNext()) {
                String group = names.next();

                if (groupId.contains(group.toLowerCase())) {
                    dep.scope = groupScopes.get(group).asText();
                    return;
                }
            }
        }
    }

    // ======================================================
    // ANNOTATION PROCESSORS
    // ======================================================

    private static void applyAnnotationProcessorRules(String artifactId,
                                                      DependencyConfig dep) {

        JsonNode processors = ROOT.get("annotationProcessors");

        if (processors == null) return;

        Iterator<String> names = processors.fieldNames();

        while (names.hasNext()) {
            String key = names.next();

            if (!artifactId.contains(key.toLowerCase())) continue;

            JsonNode node = processors.get(key);

            dep.annotationProcessor = true;

            if (node.has("artifact")) {
                dep.processorArtifact =
                        node.get("artifact").asText();
            }

            if (node.has("optional") &&
                node.get("optional").asBoolean()) {

                dep.optional = true;
            }

            return;
        }
    }

    // ======================================================
    // OPTIONAL
    // ======================================================

    private static void applyOptionalRules(String artifactId,
                                           DependencyConfig dep) {

        JsonNode optional = ROOT.get("optional");

        if (optional == null) return;

        for (JsonNode node : optional) {
            if (artifactId.contains(node.asText().toLowerCase())) {
                dep.optional = true;
                return;
            }
        }
    }

    public static String getProcessorArtifact(String artifactId) {

        if (ROOT == null) return null;

        JsonNode processors = ROOT.get("annotationProcessors");

        if (processors == null) return null;

        Iterator<String> names = processors.fieldNames();

        while (names.hasNext()) {

            String key = names.next();

            if (!artifactId.toLowerCase().contains(key.toLowerCase()))
                continue;

            JsonNode node = processors.get(key);

            if (node.has("artifact")) {
                return node.get("artifact").asText();
            }
        }

        return null;
    }
}