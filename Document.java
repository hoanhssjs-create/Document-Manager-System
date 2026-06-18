package com.documentmanager.config;

public final class DatabaseConfig {
    public static final String URL = env("DMS_DB_URL", "jdbc:sqlserver://localhost:1433;databaseName=DocumentManagerDB;encrypt=true;trustServerCertificate=true");
    public static final String USER = env("DMS_DB_USER", "sa");
    public static final String PASSWORD = env("DMS_DB_PASSWORD", "7845fa19");

    private DatabaseConfig() {
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
