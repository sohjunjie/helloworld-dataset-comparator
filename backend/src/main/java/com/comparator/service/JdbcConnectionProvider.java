package com.comparator.service;

import com.comparator.model.dto.DatabaseConnectionConfig;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface JdbcConnectionProvider {
    Connection getConnection(DatabaseConnectionConfig config) throws SQLException;
}
