package cl.biblioteca.service;

import cl.biblioteca.config.DatabaseConnection;
import cl.biblioteca.dao.*;
import cl.biblioteca.model.*;
import java.sql.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

public final class PrestamoService {
  private final DatabaseConnection db = DatabaseConnection.getInstance();
  private final BibliotecaService permisos;
  private final Usuario usuario;
  private final PrestamoDAO prestamos = new PrestamoDAO();
  private final LibroDAO libros = new LibroDAO();
  private final EstudianteDAO estudiantes = new EstudianteDAO();

  public PrestamoService(Usuario usuario) {
    this.usuario = usuario;
    permisos = new BibliotecaService(usuario);
  }

  
  public synchronized Prestamo prestar(int estudiante, int libro) throws SQLException {
    return db.transaction(
        () -> {
          permisos.autorizarEstudiante(estudiante);
          if (estudiantes.buscar(estudiante) == null)
            throw new IllegalArgumentException("Selecciona un estudiante válido.");
          // UPDATE condicional protege el último ejemplar incluso entre procesos.
          if (!libros.descontarStock(libro))
            throw new IllegalArgumentException("No quedan ejemplares disponibles.");
          Prestamo p = new Prestamo();
          p.setIdEstudiante(estudiante);
          p.setIdLibro(libro);
          p.setFechaPrestamo(LocalDate.now());
          p.setFechaDevolucion(LocalDate.now().plusDays(7));
          p.setDevuelto(false);
          prestamos.crear(p); 
          return p;
        });
  }

  public synchronized Prestamo devolver(int id) throws SQLException {
    return db.transaction(
        () -> {
          Prestamo p = prestamos.bloquear(id);
          if (p == null) throw new IllegalArgumentException("Selecciona un préstamo válido.");
          permisos.autorizarEstudiante(p.getIdEstudiante());
          if (p.getDevuelto()) throw new IllegalArgumentException("Este préstamo ya fue devuelto.");
          p.setDevuelto(true);
          p.setFechaDevolucionReal(LocalDate.now());
          p.setDiasAtraso(
              (int) Math.max(0, ChronoUnit.DAYS.between(p.getFechaDevolucion(), LocalDate.now())));
          prestamos.actualizar(p);
          libros.reponerStock(p.getIdLibro());
          return p;
        });
  }

  public synchronized void eliminar(int id) throws SQLException {
    permisos.exigirAdmin();
    db.transaction(
        () -> {
          Prestamo p = prestamos.bloquear(id);
          if (p == null) throw new IllegalArgumentException("Préstamo inexistente.");
          if (!p.getDevuelto()) libros.reponerStock(p.getIdLibro());
          prestamos.eliminar(id);
          return null;
        });
  }

  public List<Prestamo> listar() throws SQLException {
    return usuario.puedeGestionar()
        ? prestamos.listar()
        : prestamos.porEstudiante(permisos.estudiantePropio());
  }
}
