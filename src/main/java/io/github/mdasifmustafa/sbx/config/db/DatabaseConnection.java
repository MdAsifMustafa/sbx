package io.github.mdasifmustafa.sbx.config.db;

public final class DatabaseConnection {

	private String engine;
	private String url;
	private String host;
	private Integer port;
	private String database;
	private String username;
	private String password;

	// getters
	public String getEngine() {
		return engine;
	}

	public String getUrl() {
		return url;
	}

	public String getHost() {
		return host;
	}

	public Integer getPort() {
		return port;
	}

	public String getDatabase() {
		return database;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	/*
	 * ========================= Setters (package-private) =========================
	 */

	// setters (package-private to limit mutation)
	void setEngine(String engine) {
		this.engine = engine;
	}

	void setUrl(String url) {
		this.url = url;
	}

	void setHost(String host) {
		this.host = host;
	}

	void setPort(Integer port) {
		this.port = port;
	}

	void setDatabase(String database) {
		this.database = database;
	}

	void setUsername(String username) {
		this.username = username;
	}

	void setPassword(String password) {
		this.password = password;
	}
}