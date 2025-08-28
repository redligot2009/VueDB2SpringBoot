package com.redligot.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * Lightweight health endpoint for basic readiness checks.
 *
 * <p>When profile <code>db2</code> is active, it runs a simple
 * "SELECT 1 FROM SYSIBM.SYSDUMMY1" to verify connectivity.</p>
 */
@RestController
@RequestMapping("/health")
public class HealthController {

	private final DataSource dataSource;

	public HealthController(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@GetMapping
	public ResponseEntity<Map<String, Object>> health() {
		Map<String, Object> body = new HashMap<>();
		body.put("status", "UP");

		String active = System.getProperty("spring.profiles.active", System.getenv("SPRING_PROFILES_ACTIVE"));
		if (active != null && active.contains("db2")) {
			Map<String, Object> db = new HashMap<>();
			try (Connection conn = dataSource.getConnection();
			     Statement st = conn.createStatement();
			     ResultSet rs = st.executeQuery("SELECT 1 FROM SYSIBM.SYSDUMMY1")) {
				if (rs.next()) {
					db.put("status", "UP");
					db.put("result", rs.getInt(1));
				}
			} catch (Exception ex) {
				db.put("status", "DOWN");
				db.put("error", ex.getMessage());
				body.put("status", "DEGRADED");
			}
			body.put("db2", db);
		}

		return ResponseEntity.ok(body);
	}
}
