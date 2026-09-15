package com.gbhackathon.AICareerCode.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${DATABASE_URL:${spring.datasource.url:}}")
    private String dbUrl;

    @Value("${spring.datasource.username:${DATABASE_USERNAME:}}")
    private String defaultUsername;

    @Value("${spring.datasource.password:${DATABASE_PASSWORD:}}")
    private String defaultPassword;

    @Value("${spring.datasource.driver-class-name:${DATABASE_DRIVER:}}")
    private String defaultDriver;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setConnectionTimeout(30000);
        ds.setMaximumPoolSize(5);

        if (dbUrl != null && (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://"))) {
            try {
                log.info("Detected Render Cloud PostgreSQL URL, formatting for JDBC...");
                URI uri = new URI(dbUrl.replace("postgres://", "postgresql://"));
                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                String path = uri.getPath();
                String query = uri.getQuery();
                String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path + (query != null && !query.isBlank() ? "?" + query : "");

                ds.setJdbcUrl(jdbcUrl);
                ds.setDriverClassName("org.postgresql.Driver");
                System.setProperty("jakarta.persistence.jdbc.url", jdbcUrl);

                if (uri.getUserInfo() != null) {
                    String[] parts = uri.getUserInfo().split(":");
                    ds.setUsername(parts[0]);
                    if (parts.length > 1) {
                        ds.setPassword(parts[1]);
                    }
                }
                log.info("Successfully configured PostgreSQL JDBC: {}", jdbcUrl);
                return ds;
            } catch (Exception e) {
                log.error("Failed to parse PostgreSQL URL: {}", e.getMessage());
            }
        }

        if (dbUrl != null && dbUrl.startsWith("jdbc:postgresql:")) {
            ds.setJdbcUrl(dbUrl);
            ds.setDriverClassName("org.postgresql.Driver");
            System.setProperty("jakarta.persistence.jdbc.url", dbUrl);
            if (defaultUsername != null && !defaultUsername.isBlank()) ds.setUsername(defaultUsername);
            if (defaultPassword != null && !defaultPassword.isBlank()) ds.setPassword(defaultPassword);
            return ds;
        }

        // Standard JDBC URL (MySQL or local fallback)
        ds.setJdbcUrl(dbUrl);
        if (defaultUsername != null && !defaultUsername.isBlank()) ds.setUsername(defaultUsername);
        if (defaultPassword != null && !defaultPassword.isBlank()) ds.setPassword(defaultPassword);
        if (defaultDriver != null && !defaultDriver.isBlank()) {
            ds.setDriverClassName(defaultDriver);
        } else if (dbUrl != null && dbUrl.startsWith("jdbc:mysql:")) {
            ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        }
        return ds;
    }
}
