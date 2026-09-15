package com.gbhackathon.AICareerCode;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import com.zaxxer.hikari.HikariDataSource;
import static org.junit.jupiter.api.Assertions.*;

class AiCareerCodeApplicationTests {

	@Test
	void testPostgreSqlUrlParsing() {
		String dbUrl = "postgresql://aicareer_user:757RyiPqnvjXrk1MU66LnQPZ1YYuSVJi@dpg-dakin47qj5pc73baotrg-a/aicareer_db";
		URI uri = URI.create(dbUrl.replace("postgres://", "postgresql://"));
		assertEquals("dpg-dakin47qj5pc73baotrg-a", uri.getHost());
		assertEquals(-1, uri.getPort());
		assertEquals("/aicareer_db", uri.getPath());
		assertEquals("aicareer_user:757RyiPqnvjXrk1MU66LnQPZ1YYuSVJi", uri.getUserInfo());

		String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + (uri.getPort() == -1 ? 5432 : uri.getPort()) + uri.getPath();
		assertEquals("jdbc:postgresql://dpg-dakin47qj5pc73baotrg-a:5432/aicareer_db", jdbcUrl);

		HikariDataSource ds = new HikariDataSource();
		ds.setJdbcUrl(jdbcUrl);
		ds.setDriverClassName("org.postgresql.Driver");
		String[] parts = uri.getUserInfo().split(":");
		ds.setUsername(parts[0]);
		ds.setPassword(parts[1]);

		assertEquals("org.postgresql.Driver", ds.getDriverClassName());
		assertEquals("jdbc:postgresql://dpg-dakin47qj5pc73baotrg-a:5432/aicareer_db", ds.getJdbcUrl());
		assertEquals("aicareer_user", ds.getUsername());
		ds.close();

		assertDoesNotThrow(() -> Class.forName("org.hibernate.dialect.PostgreSQLDialect"));
	}
}
