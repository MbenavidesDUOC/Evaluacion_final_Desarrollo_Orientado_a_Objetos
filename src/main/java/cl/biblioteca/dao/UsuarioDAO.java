package cl.biblioteca.dao;

import cl.biblioteca.model.*;
import java.sql.*;
import java.util.*;

public final class UsuarioDAO extends BaseDAO<Usuario> {
  public UsuarioDAO() {
    super("usuarios", "nombre,rut,correo,`contraseña`,rol");
  }

  protected Usuario map(ResultSet rs) throws SQLException {
    Usuario u =
        "bibliotecario".equals(rs.getString("rol")) ? new Bibliotecario() : new UsuarioEstudiante();
    u.setId(rs.getInt("id"));
    u.setNombre(rs.getString("nombre"));
    u.setRut(rs.getString("rut"));
    u.setCorreo(rs.getString("correo"));
    u.setClave(rs.getString("contraseña"));
    return u;
  }

  protected void bind(PreparedStatement ps, Usuario u) throws SQLException {
    ps.setString(1, u.getNombre());
    ps.setString(2, u.getRut());
    ps.setString(3, u.getCorreo());
    ps.setString(4, u.getClave());
    ps.setString(5, u.getRol());
  }

  public Usuario porRut(String rut) throws SQLException {
    List<Usuario> rows = consultar("SELECT * FROM usuarios WHERE rut=?", rut);
    return rows.isEmpty() ? null : rows.get(0);
  }

  public List<Usuario> porCorreo(String correo) throws SQLException {
    return consultar("SELECT * FROM usuarios WHERE correo=?", correo);
  }
}
