package cl.biblioteca.config;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.Properties;

public final class DatabaseConnection {
  private static final DatabaseConnection INSTANCE = new DatabaseConnection();
  private Connection connection;

  private DatabaseConnection() {}

  public static DatabaseConnection getInstance() {
    return INSTANCE;
  }

  public synchronized Connection getConnection() throws SQLException {
    if (connection == null || connection.isClosed()) {
      Properties p = new Properties();
      String file = System.getProperty("biblioteca.config", "db.properties");
      try (InputStream in = Files.newInputStream(Path.of(file))) {
        p.load(in);
      } catch (IOException e) {
        throw new SQLException(
            "Falta db.properties. Copia db.properties.example y configura MySQL.", e);
      }
      connection =
          DriverManager.getConnection(
              p.getProperty("db.url"), p.getProperty("db.user"), p.getProperty("db.password"));
    }
    return connection;
  }

  public synchronized void close() throws SQLException {
    if (connection != null) connection.close();
  }

  @FunctionalInterface
  public interface Transaction<T> {
    T run() throws SQLException;
  }

  public synchronized <T> T transaction(Transaction<T> task) throws SQLException {
    Connection c = getConnection();
    boolean old = c.getAutoCommit();
    c.setAutoCommit(false);
    try {
      T result = task.run();
      c.commit();
      return result;
    } catch (SQLException | RuntimeException e) {
      try {
        c.rollback();
      } catch (SQLException rollback) {
        e.addSuppressed(rollback);
      }
      throw e;
    } finally {
      c.setAutoCommit(old);
    }
  }
}
