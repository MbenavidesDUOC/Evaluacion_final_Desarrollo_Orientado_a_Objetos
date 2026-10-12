package cl.biblioteca.dao;

import cl.biblioteca.model.*;
import java.sql.*;

public final class LibroDAO extends BaseDAO<Libro> {
  public LibroDAO() {
    super("libros", "titulo,autor,isbn,editorial,stock,id_categoria");
  }

  protected Libro map(ResultSet rs) throws SQLException {
    Libro l = new Libro();
    l.setId(rs.getInt("id"));
    l.setTitulo(rs.getString("titulo"));
    l.setAutor(rs.getString("autor"));
    l.setIsbn(rs.getString("isbn"));
    l.setEditorial(rs.getString("editorial"));
    l.setStock(rs.getInt("stock"));
    l.setIdCategoria(rs.getInt("id_categoria"));
    return l;
  }

  protected void bind(PreparedStatement ps, Libro l) throws SQLException {
    ps.setString(1, l.getTitulo());
    ps.setString(2, l.getAutor());
    ps.setString(3, l.getIsbn());
    ps.setString(4, l.getEditorial());
    ps.setInt(5, l.getStock());
    ps.setInt(6, l.getIdCategoria());
  }

  public boolean descontarStock(int id) throws SQLException {
    synchronized (db) {
      try (PreparedStatement ps =
          db.getConnection()
              .prepareStatement("UPDATE libros SET stock=stock-1 WHERE id=? AND stock>0")) {
        ps.setInt(1, id);
        return ps.executeUpdate() == 1;
      }
    }
  }

  public void reponerStock(int id) throws SQLException {
    synchronized (db) {
      try (PreparedStatement ps =
          db.getConnection().prepareStatement("UPDATE libros SET stock=stock+1 WHERE id=?")) {
        ps.setInt(1, id);
        if (ps.executeUpdate() != 1) throw new SQLException("Libro inexistente.");
      }
    }
  }
}
