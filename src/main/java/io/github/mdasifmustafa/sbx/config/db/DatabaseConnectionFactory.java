package io.github.mdasifmustafa.sbx.config.db;

public final class DatabaseConnectionFactory {

    private DatabaseConnectionFactory() {}

    /* =========================
       YAML connection
       ========================= */
    public static DatabaseConnection forYaml(
        String engine,
        String host,
        int port,
        String database,
        String username,
        String password
    ) {
        DatabaseConnection conn = new DatabaseConnection();
        conn.setEngine(engine);
        conn.setHost(host);
        conn.setPort(port);
        conn.setDatabase(database);
        conn.setUsername(username);
        conn.setPassword(password);
        return conn;
    }

    /* =========================
       Properties (JDBC) connection
       ========================= */
    public static DatabaseConnection forJdbc(
        String url,
        String username,
        String password
    ) {
        DatabaseConnection conn = new DatabaseConnection();
        conn.setUrl(url);
        conn.setUsername(username);
        conn.setPassword(password);
        return conn;
    }
    
    public static DatabaseConnection forEngine(
    	    String engineKey,
    	    DatabaseEngine engine,
    	    String host,
    	    Integer port,
    	    String database,
    	    String username,
    	    String password
    	) {
    	    String url;

    	    switch (engineKey) {

    	        case "sqlite":
    	            url = engine.jdbcPrefix + database;
    	            break;

    	        case "h2":
    	            // database contains full h2 path already
    	            url = engine.jdbcPrefix + database;
    	            break;

    	        case "oracle":
    	            url = engine.jdbcPrefix +
    	                  host + ":" + port + "/" + database;
    	            break;

    	        case "sqlserver":
    	            url = engine.jdbcPrefix +
    	                  host + ":" + port +
    	                  ";databaseName=" + database;
    	            break;

    	        default:
    	            // mysql, postgres, mariadb, etc
    	            url = engine.jdbcPrefix +
    	                  host + ":" + port + "/" + database;
    	    }

    	    DatabaseConnection conn = new DatabaseConnection();
    	    conn.setUrl(url);
    	    conn.setUsername(username);
    	    conn.setPassword(password);

    	    return conn;
    	}
}