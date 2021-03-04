package com.intolighter.appealssystem.persistence.repositories;

import org.springframework.jdbc.core.JdbcTemplate;

public abstract class RepositoryUtils {

    protected JdbcTemplate jdbcTemplate;
    private String databaseName;

    public RepositoryUtils(JdbcTemplate jdbcTemplate, String databaseName) {
        this.jdbcTemplate = jdbcTemplate;
        this.databaseName = databaseName;
    }

    @SuppressWarnings("ConstantConditions")
    protected <T> boolean checkForExistence(String paramName, T paramValue) {
        return jdbcTemplate.queryForObject(
                String.format("SELECT EXISTS(SELECT * FROM %s WHERE %s = %s)", databaseName, paramName, "'" + paramValue + "'"),
                Boolean.class);
    }
}
