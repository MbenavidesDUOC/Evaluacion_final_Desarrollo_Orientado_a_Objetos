package cl.biblioteca.dao;

import cl.biblioteca.model.*;
import java.sql.*;
import java.util.*;

public final class PrestamoDAO extends BaseDAO<Prestamo> {
  public PrestamoDAO() {
    super(
        "prestamos",
        "id_estudiante,id_libro,fecha_prestamo,fecha_devolucion,devuelto,fecha_devolucion_real,dias_atraso");
  }

  protected Prestamo map(ResultSet rs) throws SQLException {
    Prestamo p = new Prestamo();
    p.setId(rs.getInt("id"));
    p.setIdEstudiante(rs.getInt("id_estudiante"));
    p.setIdLibro(rs.getInt("id_libro"));
    p.setFechaPrestamo(rs.getDate("fecha_prestamo").toLocalDate());
    p.setFechaDevolucion(rs.getDate("fecha_devolucion").toLocalDate());
    p.setDevuelto(rs.getBoolean("devuelto"));
    java.sql.Date real = rs.getDate("fecha_devolucion_real");
    p.setFechaDevolucionReal(real == null ? null : real.toLocalDate());
    p.setDiasAtraso(rs.getInt("dias_atraso"));
    return p;
  }

  protected void bind(PreparedStatement ps, Prestamo p) throws SQLException {
    ps.setInt(1, p.getIdEstudiante());
    ps.setInt(2, p.getIdLibro());
    ps.setDate(3, java.sql.Date.valueOf(p.getFechaPrestamo()));
    ps.setDate(4, java.sql.Date.valueOf(p.getFechaDevolucion()));
    ps.setBoolean(5, p.getDevuelto());
    ps.setDate(
        6,
        p.getFechaDevolucionReal() == null
            ? null
            : java.sql.Date.valueOf(p.getFechaDevolucionReal()));
    ps.setInt(7, p.getDiasAtraso());
  }

  public Prestamo bloquear(int id) throws SQLException {
    List<Prestamo> rows = consultar("SELECT * FROM prestamos WHERE id=? FOR UPDATE", id);
    return rows.isEmpty() ? null : rows.get(0);
  }

  public List<Prestamo> porEstudiante(int id) throws SQLException {
    return consultar("SELECT * FROM prestamos WHERE id_estudiante=? ORDER BY id DESC", id);
  }
}
