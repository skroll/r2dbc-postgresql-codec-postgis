package com.github.skroll.r2dbc.postgresql.codec.postgis.util;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import reactor.util.annotation.Nullable;

import java.io.IOException;
import java.net.Socket;
import java.util.function.Supplier;

public final class PostgresqlServerExtension implements BeforeAllCallback, AfterAllCallback {
  private volatile PostgreSQLContainer<?> containerInstance = null;

  private final Supplier<PostgreSQLContainer<?>> container = () -> {
    if (this.containerInstance != null) {
      return this.containerInstance;
    }
    return this.containerInstance = new PostgreSQLContainer<>("mdillon/postgis:10");
  };

  private final DatabaseContainer postgres = new TestContainer(this.container.get());

  private final boolean useTestContainer = this.postgres instanceof TestContainer;

  private HikariDataSource dataSource;

  private JdbcOperations jdbcOperations;

  @Override
  public void afterAll(ExtensionContext context) {
    this.dataSource.close();
    if (this.useTestContainer) {
      this.container.get().stop();
    }
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    if (this.useTestContainer) {
      this.container.get().start();
    }

    HikariDataSource hikariDataSource = new HikariDataSource();
    hikariDataSource.setUsername(getUsername());
    hikariDataSource.setPassword(getPassword());
    hikariDataSource.setJdbcUrl(String.format("jdbc:postgresql://%s:%d/%s", getHost(), getPort(), getDatabase()));

    this.dataSource = hikariDataSource;

    this.dataSource.setMaximumPoolSize(1);

    this.jdbcOperations = new JdbcTemplate(this.dataSource);
  }

  public String getDatabase() {
    return this.postgres.getDatabase();
  }

  @Nullable
  public JdbcOperations getJdbcOperations() {
    return this.jdbcOperations;
  }

  public String getHost() {
    return this.postgres.getHost();
  }

  public int getPort() {
    return this.postgres.getPort();
  }

  public String getUsername() {
    return this.postgres.getUsername();
  }

  public String getPassword() {
    return this.postgres.getPassword();
  }

  /**
   * Interface to be implemented by database providers (provided database, test container).
   */
  interface DatabaseContainer {

    String getHost();

    int getPort();

    String getDatabase();

    String getUsername();

    String getPassword();
  }

  /**
   * Externally provided Postgres instance.
   */
  static class External implements DatabaseContainer {

    public static final External INSTANCE = new External();

    @Override
    public String getHost() {
      return "localhost";
    }

    @Override
    public int getPort() {
      return 5432;
    }

    @Override
    public String getDatabase() {
      return "postgres";
    }

    @Override
    public String getUsername() {
      return "postgres";
    }

    @Override
    public String getPassword() {
      return "postgres";
    }

    /**
     * Returns whether this container is available.
     *
     * @return
     */
    @SuppressWarnings("try")
    boolean isAvailable() {

      try (Socket ignored = new Socket(getHost(), getPort())) {

        return true;
      } catch (IOException e) {
        return false;
      }
    }
  }

  /**
   * {@link DatabaseContainer} provided by {@link JdbcDatabaseContainer}.
   */
  static class TestContainer implements DatabaseContainer {

    private final JdbcDatabaseContainer<?> container;

    TestContainer(JdbcDatabaseContainer<?> container) {
      this.container = container;
    }

    @Override
    public String getHost() {
      return this.container.getContainerIpAddress();
    }

    @Override
    public int getPort() {
      return this.container.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT);
    }

    @Override
    public String getDatabase() {
      return this.container.getDatabaseName();
    }

    @Override
    public String getUsername() {
      return this.container.getUsername();
    }

    @Override
    public String getPassword() {
      return this.container.getPassword();
    }
  }
}
