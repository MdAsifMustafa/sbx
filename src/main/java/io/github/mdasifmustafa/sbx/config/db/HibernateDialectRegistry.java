package io.github.mdasifmustafa.sbx.config.db;

import java.util.Map;

public final class HibernateDialectRegistry {

    private static final Map<String, String> DIALECTS = Map.of(
        "sqlite", "org.hibernate.community.dialect.SQLiteDialect",
        "postgresql", "org.hibernate.dialect.PostgreSQLDialect",
        "mysql", "org.hibernate.dialect.MySQLDialect",
        "mariadb", "org.hibernate.dialect.MariaDBDialect",
        "h2", "org.hibernate.dialect.H2Dialect",
        "sqlserver", "org.hibernate.dialect.SQLServerDialect",
        "oracle", "org.hibernate.dialect.OracleDialect"
    );

    public static String getDialect(String engine) {
        return DIALECTS.get(engine);
    }

    private HibernateDialectRegistry() {}
}