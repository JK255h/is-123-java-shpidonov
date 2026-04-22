package com.example.survey.config;

import org.firebirdsql.management.FBManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.io.File;
import java.net.URI;
import java.sql.Connection;

@Configuration
public class DatabaseInitializer {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    public CommandLineRunner initDatabase(DataSource dataSource) {
        return args -> {
            String path = extractPathFromJdbcUrl(dbUrl);
            File dbFile = new File(path);

            if (!dbFile.exists()) {
                System.out.println("Database file not found at: " + path);
                System.out.println("Creating new Firebird database...");

                FBManager manager = new FBManager();
                manager.setServer("localhost");
                manager.setPort(3050);
                manager.setUserName(username);
                manager.setPassword(password);
                manager.start();
                
                try {
                    manager.createDatabase(path, username, password);
                    System.out.println("Database created successfully.");

                    // Initialize schema and seed data
                    ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                    populator.addScript(new ClassPathResource("OPROS_CREATE.sql"));
                    populator.addScript(new ClassPathResource("seed_admin.sql"));
                    
                    try (Connection connection = dataSource.getConnection()) {
                        populator.populate(connection);
                        System.out.println("Schema and seed data initialized.");
                    }
                } catch (Exception e) {
                    System.err.println("Error creating database: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    manager.stop();
                }
            } else {
                System.out.println("Database file found at: " + path);
            }
        };
    }

    private String extractPathFromJdbcUrl(String url) {
        // Expected format: jdbc:firebirdsql://localhost:3050/path/to/db?encoding=UTF8
        // We need to extract: path/to/db
        String prefix = "jdbc:firebirdsql://localhost:3050/";
        if (url.startsWith(prefix)) {
            String pathWithParams = url.substring(prefix.length());
            int queryIdx = pathWithParams.indexOf('?');
            return queryIdx != -1 ? pathWithParams.substring(0, queryIdx) : pathWithParams;
        }
        return url; // fallback
    }
}
