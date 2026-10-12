package cl.biblioteca.dao;

import cl.biblioteca.config.DatabaseConnection;
import cl.biblioteca.model.Entidad;
import java.sql.*;
import java.util.*;

public abstract class BaseDAO<T extends Entidad> implements CrudDAO<T> {
  protected final DatabaseConnection db = DatabaseConnection.getInstance();
  private final String tabla, columnas;

  protected BaseDAO(String tabla, String columnas) {
    this.tabla = tabla;
    this.columnas = columnas;
  }

  protected abstract T map(ResultSet rs) throws SQLException;

  protected abstract void bind(PreparedStatement ps, T t) throws SQLException;

  protected int cantidad() {
    return columnas.split(",").length;
  }

  public List<T> listar() throws SQLException {
    return consultar("SELECT * FROM " + tabla + " ORDER BY id");
  }

  public T buscar(int id) throws SQLException {
    List<T> rows = consultar("SELECT * FROM " + tabla + " WHERE id=?", id);
    return rows.isEmpty() ? null : rows.get(0);
  }

  protected List<T> consultar(String sql, Object... args) throws SQLException {
    synchronized (db) {
      try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
        for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);
        try (ResultSet rs = ps.executeQuery()) {
          List<T> rows = new ArrayList<>();
          while (rs.next()) rows.add(map(rs));
          return rows;
        }
      }
    }
  }

  public void crear(T t) throws SQLException {
    synchronized (db) {
      String marks = String.join(",", Collections.nCopies(cantidad(), "?"));
      try (PreparedStatement ps =
          db.getConnection()
              .prepareStatement(
                  "INSERT INTO " + tabla + " (" + columnas + ") VALUES (" + marks + ")",
                  Statement.RETURN_GENERATED_KEYS)) {
        bind(ps, t);
        ps.executeUpdate();
        try (ResultSet rs = ps.getGeneratedKeys()) {
          if (rs.next()) t.setId(rs.getInt(1));
        }
      }
    }
  }

  public void actualizar(T t) throws SQLException {
    synchronized (db) {
      StringJoiner sets = new StringJoiner(",");
      for (String c : columnas.split(",")) sets.add(c + "=?");
      try (PreparedStatement ps =
          db.getConnection().prepareStatement("UPDATE " + tabla + " SET " + sets + " WHERE id=?")) {
        bind(ps, t);
        ps.setInt(cantidad() + 1, t.getId());
        if (ps.executeUpdate() != 1) throw new SQLException("El registro ya no existe.");
      }
    }
  }

  public void eliminar(int id) throws SQLException {
    synchronized (db) {
      try (PreparedStatement ps =
          db.getConnection().prepareStatement("DELETE FROM " + tabla + " WHERE id=?")) {
        ps.setInt(1, id);
        if (ps.executeUpdate() != 1) throw new SQLException("El registro ya no existe.");
      }
    }
  }
}
