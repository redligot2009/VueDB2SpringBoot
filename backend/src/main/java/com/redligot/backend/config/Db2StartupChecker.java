package com.redligot.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Component that checks DB2 connectivity on application startup.
 * Only active when the 'db2' profile is enabled.
 */
@Component
@Profile("db2")
public class Db2StartupChecker implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(Db2StartupChecker.class);
	private final DataSource dataSource;

	public Db2StartupChecker(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void run(String... args) throws Exception {
		logger.info("Checking DB2 connectivity...");
		
		try (Connection connection = dataSource.getConnection();
			 Statement statement = connection.createStatement();
			 ResultSet resultSet = statement.executeQuery("SELECT 1 FROM SYSIBM.SYSDUMMY1")) {
			
			if (resultSet.next()) {
				int result = resultSet.getInt(1);
				logger.info("✅ DB2 connection successful! Test query returned: {}", result);
			} else {
				logger.warn("⚠️ DB2 connection established but test query returned no results");
			}
		} catch (Exception e) {
			logger.error("❌ DB2 connection failed: {}", e.getMessage());
			// Don't throw the exception - let the application continue
			// The connection pool will retry automatically
		}
	}
}
