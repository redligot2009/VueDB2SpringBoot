package com.redligot.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Performs a quick DB2 connectivity check at application startup when the
 * {@code db2} Spring profile is active. Fails fast if the database is not
 * reachable or a basic query cannot execute.
 */
@Profile("db2")
@Component
public class Db2StartupChecker implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(Db2StartupChecker.class);

	private final DataSource dataSource;

	public Db2StartupChecker(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		try (Connection conn = dataSource.getConnection();
			 Statement st = conn.createStatement();
			 ResultSet rs = st.executeQuery("SELECT 1 FROM SYSIBM.SYSDUMMY1")) {
			if (rs.next()) {
				log.info("DB2 startup check succeeded (result={})", rs.getInt(1));
			}
		} catch (Exception ex) {
			log.error("DB2 startup check failed: {}", ex.getMessage(), ex);
			throw new IllegalStateException("DB2 connection test failed during startup", ex);
		}
	}
}
