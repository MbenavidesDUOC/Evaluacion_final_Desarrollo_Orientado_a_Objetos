package cl.biblioteca.service;

import cl.biblioteca.config.DatabaseConnection;
import cl.biblioteca.dao.*;
import cl.biblioteca.model.*;
import java.sql.*;
import java.util.*;

public final class AuthService {
  public Usuario ingresar(String identificador, String clave) throws SQLException {
    synchronized (DatabaseConnection.getInstance()) {
      UsuarioDAO dao = new UsuarioDAO();
      Usuario u = dao.porRut(identificador);
      if (u == null) {
        List<Usuario> users = dao.porCorreo(identificador);
        if (users.size() == 1) u = users.get(0);
      }
      if (u == null || !PasswordService.verificar(clave, u.getClave()))
        throw new IllegalArgumentException("Usuario o contraseña incorrectos.");
      if (!u.puedeGestionar() && new EstudianteDAO().porRut(u.getRut()) == null)
        throw new IllegalArgumentException(
            "Esta cuenta no está vinculada a un estudiante. Solicita revisión al bibliotecario.");
      return u;
    }
  }
}
