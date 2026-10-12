package cl.biblioteca.dao;

import cl.biblioteca.model.*;
import java.sql.*;

public final class CategoriaDAO extends BaseDAO<Categoria> {
  public CategoriaDAO() {
    super("categorias", "nombre");
  }

  protected Categoria map(ResultSet rs) throws SQLException {
    Categoria c = new Categoria();
    c.setId(rs.getInt("id"));
    c.setNombre(rs.getString("nombre"));
    return c;
  }

  protected void bind(PreparedStatement ps, Categoria c) throws SQLException {
    ps.setString(1, c.getNombre());
  }
}
