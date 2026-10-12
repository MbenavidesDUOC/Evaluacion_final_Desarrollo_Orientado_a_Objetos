package cl.biblioteca.dao;

import cl.biblioteca.config.DatabaseConnection;
import java.sql.*;
import java.util.*;

public final class ReporteDAO {
  private final DatabaseConnection db = DatabaseConnection.getInstance();

  public List<Object[]> masPrestados() throws SQLException {
    return consultar(
        "SELECT l.titulo,l.autor,COUNT(p.id) AS total FROM libros l LEFT JOIN prestamos p ON"
            + " p.id_libro=l.id GROUP BY l.id,l.titulo,l.autor ORDER BY total DESC,l.titulo");
  }

  public List<Object[]> historial(int estudiante) throws SQLException {
    return consultar(
        "SELECT p.id,e.nombre,l.titulo,p.fecha_prestamo,p.fecha_devolucion,CASE WHEN p.devuelto"
            + " THEN 'Devuelto' ELSE 'Pendiente' END,p.fecha_devolucion_real,CASE WHEN p.devuelto"
            + " AND p.fecha_devolucion_real IS NULL THEN NULL WHEN p.devuelto THEN p.dias_atraso"
            + " ELSE GREATEST(TIMESTAMPDIFF(DAY,p.fecha_devolucion,CURDATE()),0) END FROM prestamos"
            + " p JOIN estudiantes e ON p.id_estudiante=e.id JOIN libros l ON p.id_libro=l.id WHERE"
            + " e.id=? ORDER BY p.id DESC",
        estudiante);
  }

  public List<Object[]> activos(Integer estudiante) throws SQLException {
    String sql =
        "SELECT"
            + " p.id,e.nombre,l.titulo,p.fecha_prestamo,p.fecha_devolucion,GREATEST(TIMESTAMPDIFF(DAY,p.fecha_devolucion,CURDATE()),0)"
            + " FROM prestamos p JOIN estudiantes e ON p.id_estudiante=e.id JOIN libros l ON"
            + " p.id_libro=l.id WHERE p.devuelto=FALSE";
    return estudiante == null
        ? consultar(sql + " ORDER BY p.fecha_devolucion")
        : consultar(sql + " AND e.id=? ORDER BY p.fecha_devolucion", estudiante);
  }

  private List<Object[]> consultar(String sql, Object... args) throws SQLException {
    synchronized (db) {
      try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
        for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);
        try (ResultSet rs = ps.executeQuery()) {
          List<Object[]> rows = new ArrayList<>();
          int cols = rs.getMetaData().getColumnCount();
          while (rs.next()) {
            Object[] r = new Object[cols];
            for (int i = 0; i < cols; i++) r[i] = rs.getObject(i + 1);
            rows.add(r);
          }
          return rows;
        }
      }
    }
  }
}
