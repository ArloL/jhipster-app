package io.github.arlol.app.config;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;

public class PostgreSqlTestContainer implements SqlTestContainer {

    private static final Logger log = LoggerFactory.getLogger(PostgreSqlTestContainer.class);

    private PostgreSQLContainer<?> postgreSQLContainer;

    @Override
    public void destroy() {
        if (null != postgreSQLContainer && postgreSQLContainer.isRunning()) {
            postgreSQLContainer.stop();
        }
    }

    @SuppressWarnings("resource")
	@Override
    public void afterPropertiesSet() {
        if (null == postgreSQLContainer) {
        	WaitAllStrategy waitStrategy = new WaitAllStrategy()
        	    .withStartupTimeout(Duration.of(60, ChronoUnit.SECONDS))
        	    .withStrategy(Wait.forListeningPort())
        	    .withStrategy(Wait.forLogMessage(".*database system is ready to accept connections.*\\s", 2));
            postgreSQLContainer =
                new PostgreSQLContainer<>("postgres:14.5")
                    .withDatabaseName("app")
                    .withTmpFs(Collections.singletonMap("/testtmpfs", "rw"))
                    .withLogConsumer(new Slf4jLogConsumer(log))
                    .waitingFor(waitStrategy)
                    .withReuse(true);
        }
        if (!postgreSQLContainer.isRunning()) {
            postgreSQLContainer.start();
        }
    }

    @Override
    public JdbcDatabaseContainer<?> getTestContainer() {
        return postgreSQLContainer;
    }
}
