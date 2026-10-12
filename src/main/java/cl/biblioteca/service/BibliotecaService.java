package cl.biblioteca.service;

import cl.biblioteca.config.DatabaseConnection;
import cl.biblioteca.dao.*;
import cl.biblioteca.model.*;
import java.sql.*;
import java.util.*;

public final class BibliotecaService {
  private final Usuario usuario;
  private final DatabaseConnection db = DatabaseConnection.getInstance();
  private final LibroDAO libros = new LibroDAO();
  private final CategoriaDAO categorias = new CategoriaDAO();
  private final EstudianteDAO estudiantes = new EstudianteDAO();
  private final UsuarioDAO usuarios = new UsuarioDAO();

  public BibliotecaService(Usuario usuario) {
    this.usuario = usuario;
  }

  public void exigirAdmin() {
    if (!usuario.puedeGestionar())
      throw new IllegalArgumentException("Solo el bibliotecario puede gestionar estos datos.");
  }

  public int estudiantePropio() throws SQLException {
    Estudiante e = estudiantes.porRut(usuario.getRut());
    if (e == null) throw new IllegalArgumentException("No existe estudiante vinculado a tu RUT.");
    return e.getId();
  }

  public void autorizarEstudiante(int id) throws SQLException {
    if (!usuario.puedeGestionar() && id != estudiantePropio())
      throw new IllegalArgumentException("Solo puedes consultar y operar tus propios préstamos.");
  }

  public List<Libro> libros() throws SQLException {
    return libros.listar();
  }

  public List<Categoria> categorias() throws SQLException {
    return categorias.listar();
  }

  public List<Estudiante> estudiantes() throws SQLException {
    if (usuario.puedeGestionar()) return estudiantes.listar();
    return List.of(estudiantes.buscar(estudiantePropio()));
  }

  public List<Usuario> usuarios() throws SQLException {
    exigirAdmin();
    return usuarios.listar();
  }

  public static String texto(String valor, String campo, int limite) {
    if (valor == null || valor.isBlank())
      throw new IllegalArgumentException("Completa " + campo + ".");
    valor = valor.trim();
    if (valor.length() > limite)
      throw new IllegalArgumentException(campo + " admite hasta " + limite + " caracteres.");
    return valor;
  }

  private void persona(Persona p) {
    p.setNombre(texto(p.getNombre(), "nombre", 100));
    p.setRut(texto(p.getRut(), "RUT", 12).toUpperCase());
    if (!p.getRut().matches("[0-9]{7,8}-[0-9K]"))
      throw new IllegalArgumentException("Usa RUT sin puntos y con guion, por ejemplo 12345678-9.");
    p.setCorreo(texto(p.getCorreo(), "correo", 100));
    if (!p.getCorreo().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
      throw new IllegalArgumentException("Ingresa un correo válido.");
  }

  public void guardarLibro(Libro l) throws SQLException {
    exigirAdmin();
    l.setTitulo(texto(l.getTitulo(), "título", 200));
    l.setAutor(texto(l.getAutor(), "autor", 100));
    l.setIsbn(texto(l.getIsbn(), "ISBN", 20));
    l.setEditorial(texto(l.getEditorial(), "editorial", 100));
    if (l.getStock() < 0)
      throw new IllegalArgumentException("El stock disponible no puede ser negativo.");
    synchronized (db) {
      if (categorias.buscar(l.getIdCategoria()) == null)
        throw new IllegalArgumentException("Selecciona una categoría.");
      if (l.getId() == 0) libros.crear(l);
      else libros.actualizar(l);
    }
  }

  public void eliminarLibro(int id) throws SQLException {
    exigirAdmin();
    libros.eliminar(id);
  }

  public void guardarCategoria(Categoria c) throws SQLException {
    exigirAdmin();
    c.setNombre(texto(c.getNombre(), "categoría", 100));
    if (c.getId() == 0) categorias.crear(c);
    else categorias.actualizar(c);
  }

  public void eliminarCategoria(int id) throws SQLException {
    exigirAdmin();
    categorias.eliminar(id);
  }

  public void guardarEstudiante(Estudiante e, String nuevaClave) throws SQLException {
    exigirAdmin();
    persona(e);
    e.setCurso(texto(e.getCurso(), "curso", 20));
    db.transaction(
        () -> {
          Estudiante old = e.getId() == 0 ? null : estudiantes.buscar(e.getId());
          Usuario u = usuarios.porRut(old == null ? e.getRut() : old.getRut());
          Usuario target = usuarios.porRut(e.getRut());
          if (target != null && (u == null || target.getId() != u.getId()))
            throw new IllegalArgumentException("El RUT ya está asociado a otra cuenta.");
          if (u != null && u.puedeGestionar())
            throw new IllegalArgumentException("Ese RUT pertenece a un bibliotecario.");
          if (u == null) {
            u = new UsuarioEstudiante();
            u.setClave(PasswordService.hash(texto(nuevaClave, "contraseña inicial", 64)));
          } else if (!nuevaClave.isBlank())
            u.setClave(PasswordService.hash(texto(nuevaClave, "contraseña", 64)));
          u.setNombre(e.getNombre());
          u.setRut(e.getRut());
          u.setCorreo(e.getCorreo());
          if (e.getId() == 0) estudiantes.crear(e);
          else estudiantes.actualizar(e);
          if (u.getId() == 0) usuarios.crear(u);
          else usuarios.actualizar(u);
          return null;
        });
  }

  public void eliminarEstudiante(int id) throws SQLException {
    exigirAdmin();
    db.transaction(
        () -> {
          Estudiante e = estudiantes.buscar(id);
          if (e == null) throw new IllegalArgumentException("Estudiante inexistente.");
          estudiantes.eliminar(id);
          Usuario u = usuarios.porRut(e.getRut());
          if (u != null && !u.puedeGestionar()) usuarios.eliminar(u.getId());
          return null;
        });
  }

  public void guardarUsuario(Usuario u, String clave) throws SQLException {
    exigirAdmin();
    persona(u);
    db.transaction(
        () -> {
          Usuario old = u.getId() == 0 ? null : usuarios.buscar(u.getId());
          if (u.getId() == usuario.getId()
              && (!u.puedeGestionar() || !u.getRut().equals(usuario.getRut())))
            throw new IllegalArgumentException(
                "No puedes cambiar el rol o RUT de tu sesión actual.");
          if (old != null && !old.getRut().equals(u.getRut()))
            throw new IllegalArgumentException(
                "Para cambiar el RUT de un estudiante usa Gestión de estudiantes.");
          Estudiante e = estudiantes.porRut(u.getRut());
          if (!u.puedeGestionar() && e == null)
            throw new IllegalArgumentException(
                "Registra primero al estudiante en Gestión de estudiantes.");
          if (u.puedeGestionar() && e != null)
            throw new IllegalArgumentException(
                "Un RUT de estudiante no puede convertirse en bibliotecario.");
          if (clave.isBlank()) {
            if (old == null) throw new IllegalArgumentException("Ingresa una contraseña inicial.");
            u.setClave(old.getClave());
          } else u.setClave(PasswordService.hash(texto(clave, "contraseña", 64)));
          if (u.getId() == 0) usuarios.crear(u);
          else usuarios.actualizar(u);
          if (e != null) {
            e.setNombre(u.getNombre());
            e.setCorreo(u.getCorreo());
            estudiantes.actualizar(e);
          }
          return null;
        });
  }

  public void eliminarUsuario(int id) throws SQLException {
    exigirAdmin();
    if (id == usuario.getId())
      throw new IllegalArgumentException("No puedes eliminar tu sesión actual.");
    usuarios.eliminar(id);
  }
}
