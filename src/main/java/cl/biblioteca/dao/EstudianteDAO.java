package cl.biblioteca.dao;

import cl.biblioteca.model.*;
import java.sql.*;
import java.util.*;

public final class EstudianteDAO extends BaseDAO<Estudiante> {
  public EstudianteDAO() {
    super("estudiantes", "nombre,rut,curso,correo");
  }

  protected Estudiante map(ResultSet rs) throws SQLException {
    Estudiante e = new Estudiante();
    e.setId(rs.getInt("id"));
    e.setNombre(rs.getString("nombre"));
    e.setRut(rs.getString("rut"));
    e.setCurso(rs.getString("curso"));
    e.setCorreo(rs.getString("correo"));
    return e;
  }

  protected void bind(PreparedStatement ps, Estudiante e) throws SQLException {
    ps.setString(1, e.getNombre());
    ps.setString(2, e.getRut());
    ps.setString(3, e.getCurso());
    ps.setString(4, e.getCorreo());
  }

  public Estudiante porRut(String rut) throws SQLException {
    List<Estudiante> rows = consultar("SELECT * FROM estudiantes WHERE rut=?", rut);
    return rows.isEmpty() ? null : rows.get(0);
  }
}
