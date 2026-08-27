package com.comparator.service;

import com.comparator.model.dto.DatabaseConnectionConfig;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Component
public class PostgresJdbcConnectionProvider implements JdbcConnectionProvider {

    @Override
    public Connection getConnection(DatabaseConnectionConfig config) throws SQLException {
        if (config == null) {
            throw new IllegalArgumentException("DatabaseConnectionConfig cannot be null");
        }
        String host = (config.host() != null && !config.host().isBlank()) ? config.host() : "localhost";
        int port = (config.port() != null && config.port() > 0) ? config.port() : 5432;
        String database = (config.database() != null) ? config.database() : "";
        String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);

        Properties props = new Properties();
        if (config.username() != null) {
            props.setProperty("user", config.username());
        }
        if (config.password() != null) {
            props.setProperty("password", config.password());
        }
        props.setProperty("loginTimeout", "10");
        props.setProperty("connectTimeout", "10");
        props.setProperty("socketTimeout", "30");

        return DriverManager.getConnection(jdbcUrl, props);
    }
}
