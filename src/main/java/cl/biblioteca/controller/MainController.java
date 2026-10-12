package cl.biblioteca.controller;

import cl.biblioteca.dao.ReporteDAO;
import cl.biblioteca.model.*;
import cl.biblioteca.service.*;
import cl.biblioteca.view.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

public final class MainController {
  private final Usuario usuario;
  private final BibliotecaService service;
  private final PrestamoService prestamoService;
  private final MainView view;
  private final EditorPanel libros, categorias, estudiantes, usuarios;
  private final PrestamoPanel prestamos;
  private final ReportePanel reportes;
  private final JTextField titulo,
      autor,
      isbn,
      editorial,
      stock,
      catNombre,
      estNombre,
      estRut,
      curso,
      estCorreo,
      usrNombre,
      usrRut,
      usrCorreo;
  private final JPasswordField estClave, usrClave;
  private final JComboBox<Object> categoria, rol;
  private List<Libro> listaLibros = List.of();
  private List<Categoria> listaCategorias = List.of();
  private List<Estudiante> listaEstudiantes = List.of();
  private List<Usuario> listaUsuarios = List.of();
  private boolean busy;

  public MainController(Usuario usuario) {
    this.usuario = usuario;
    service = new BibliotecaService(usuario);
    prestamoService = new PrestamoService(usuario);
    view = new MainView(usuario);
    boolean admin = usuario.puedeGestionar();
    libros =
        new EditorPanel(
            "Catálogo de libros",
            new String[] {"ID", "Título", "Autor", "ISBN", "Editorial", "Disponibles", "Categoría"},
            admin);
    titulo = libros.texto("Título");
    autor = libros.texto("Autor");
    isbn = libros.texto("ISBN");
    editorial = libros.texto("Editorial");
    stock = libros.texto("Disponibles");
    categoria = libros.combo("Categoría");
    view.editor("Libros", libros);
    categorias = new EditorPanel("Gestión de categorías", new String[] {"ID", "Nombre"}, true);
    catNombre = categorias.texto("Nombre");
    estudiantes =
        new EditorPanel(
            "Gestión de estudiantes",
            new String[] {"ID", "Nombre", "RUT", "Curso", "Correo"},
            true);
    estNombre = estudiantes.texto("Nombre");
    estRut = estudiantes.texto("RUT");
    curso = estudiantes.texto("Curso");
    estCorreo = estudiantes.texto("Correo");
    estClave = estudiantes.password("Clave nueva");
    usuarios =
        new EditorPanel(
            "Gestión de usuarios", new String[] {"ID", "Nombre", "RUT", "Correo", "Rol"}, true);
    usrNombre = usuarios.texto("Nombre");
    usrRut = usuarios.texto("RUT");
    usrCorreo = usuarios.texto("Correo");
    usrClave = usuarios.password("Clave nueva");
    rol = usuarios.combo("Rol");
    rol.addItem("bibliotecario");
    rol.addItem("estudiante");
    if (admin) {
      view.editor("Estudiantes", estudiantes);
      view.editor("Categorías", categorias);
      view.editor("Usuarios", usuarios);
    }
    prestamos = new PrestamoPanel(admin);
    reportes = new ReportePanel(admin);
    view.prestamos(prestamos);
    view.reportes(reportes);
    bind();
    view.setVisible(true);
    refrescar();
  }

  @FunctionalInterface
  private interface Trabajo<T> {
    T run() throws Exception;
  }

  private <T> void ejecutar(Trabajo<T> trabajo, java.util.function.Consumer<T> resultado) {
    if (busy) return;
    busy = true;
    view.ocupado(true);
    new SwingWorker<T, Void>() {
      protected T doInBackground() throws Exception {
        return trabajo.run();
      }

      protected void done() {
        busy = false;
        view.ocupado(false);
        try {
          resultado.accept(get());
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
          Throwable cause = e.getCause();
          cause.printStackTrace();
          mensaje(Ui.error(cause), true);
        } catch (RuntimeException e) {
          mensaje(Ui.error(e), true);
        }
      }
    }.execute();
  }

  private void mensaje(String text, boolean error) {
    JOptionPane.showMessageDialog(
        view,
        text,
        error ? "Revisa los datos" : "Biblioteca escolar",
        error ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
  }

  private void validar(Runnable r) {
    try {
      r.run();
    } catch (IllegalArgumentException e) {
      mensaje(e.getMessage(), true);
    }
  }

  private int seleccionado(int id) {
    if (id == 0) throw new IllegalArgumentException("Selecciona una fila primero.");
    return id;
  }

  private int comboId(JComboBox<Object> combo) {
    if (!(combo.getSelectedItem() instanceof Entidad e))
      throw new IllegalArgumentException("Selecciona un registro de la lista.");
    return e.getId();
  }

  private int numero(String s) {
    try {
      return Integer.parseInt(s.trim());
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Disponibles debe ser un número entero.");
    }
  }

  private boolean confirmar(String texto) {
    return JOptionPane.showConfirmDialog(view, texto, "Confirmar", JOptionPane.YES_NO_OPTION)
        == JOptionPane.YES_OPTION;
  }

  private void guardar(Trabajo<Void> action, EditorPanel p) {
    ejecutar(
        action,
        x -> {
          p.limpiar();
          refrescar();
        });
  }

  private void bind() {
    for (EditorPanel p : new EditorPanel[] {libros, categorias, estudiantes, usuarios}) {
      p.actualizar.addActionListener(e -> refrescar());
      p.nuevo.addActionListener(e -> p.limpiar());
    }
    libros
        .tabla
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (!e.getValueIsAdjusting()) seleccionLibro();
            });
    categorias
        .tabla
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (!e.getValueIsAdjusting()) seleccionCategoria();
            });
    estudiantes
        .tabla
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (!e.getValueIsAdjusting()) seleccionEstudiante();
            });
    usuarios
        .tabla
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (!e.getValueIsAdjusting()) seleccionUsuario();
            });
    libros.guardar.addActionListener(
        e ->
            validar(
                () -> {
                  Libro l = new Libro();
                  l.setId(libros.idSeleccionado());
                  l.setTitulo(titulo.getText());
                  l.setAutor(autor.getText());
                  l.setIsbn(isbn.getText());
                  l.setEditorial(editorial.getText());
                  l.setStock(numero(stock.getText()));
                  l.setIdCategoria(comboId(categoria));
                  guardar(
                      () -> {
                        service.guardarLibro(l);
                        return null;
                      },
                      libros);
                }));
    categorias.guardar.addActionListener(
        e ->
            validar(
                () -> {
                  Categoria c = new Categoria();
                  c.setId(categorias.idSeleccionado());
                  c.setNombre(catNombre.getText());
                  guardar(
                      () -> {
                        service.guardarCategoria(c);
                        return null;
                      },
                      categorias);
                }));
    estudiantes.guardar.addActionListener(
        e ->
            validar(
                () -> {
                  Estudiante s = new Estudiante();
                  s.setId(estudiantes.idSeleccionado());
                  s.setNombre(estNombre.getText());
                  s.setRut(estRut.getText());
                  s.setCorreo(estCorreo.getText());
                  s.setCurso(curso.getText());
                  String clave = new String(estClave.getPassword());
                  guardar(
                      () -> {
                        service.guardarEstudiante(s, clave);
                        return null;
                      },
                      estudiantes);
                }));
    usuarios.guardar.addActionListener(
        e ->
            validar(
                () -> {
                  Usuario u =
                      "bibliotecario".equals(rol.getSelectedItem())
                          ? new Bibliotecario()
                          : new UsuarioEstudiante();
                  u.setId(usuarios.idSeleccionado());
                  u.setNombre(usrNombre.getText());
                  u.setRut(usrRut.getText());
                  u.setCorreo(usrCorreo.getText());
                  String clave = new String(usrClave.getPassword());
                  guardar(
                      () -> {
                        service.guardarUsuario(u, clave);
                        return null;
                      },
                      usuarios);
                }));
    libros.eliminar.addActionListener(
        e ->
            validar(
                () -> {
                  int id = seleccionado(libros.idSeleccionado());
                  if (confirmar(
                      "¿Eliminar el libro seleccionado? Si tiene préstamos registrados no se podrá"
                          + " eliminar."))
                    guardar(
                        () -> {
                          service.eliminarLibro(id);
                          return null;
                        },
                        libros);
                }));
    categorias.eliminar.addActionListener(
        e ->
            validar(
                () -> {
                  int id = seleccionado(categorias.idSeleccionado());
                  if (confirmar("¿Eliminar esta categoría? No puede tener libros asociados."))
                    guardar(
                        () -> {
                          service.eliminarCategoria(id);
                          return null;
                        },
                        categorias);
                }));
    estudiantes.eliminar.addActionListener(
        e ->
            validar(
                () -> {
                  int id = seleccionado(estudiantes.idSeleccionado());
                  if (confirmar(
                      "¿Eliminar el estudiante y su cuenta de acceso? Si tiene historial de"
                          + " préstamos no se podrá eliminar."))
                    guardar(
                        () -> {
                          service.eliminarEstudiante(id);
                          return null;
                        },
                        estudiantes);
                }));
    usuarios.eliminar.addActionListener(
        e ->
            validar(
                () -> {
                  int id = seleccionado(usuarios.idSeleccionado());
                  if (confirmar(
                      "¿Eliminar esta cuenta de acceso? El historial del estudiante se"
                          + " conservará."))
                    guardar(
                        () -> {
                          service.eliminarUsuario(id);
                          return null;
                        },
                        usuarios);
                }));
    prestamos.actualizar.addActionListener(e -> refrescar());
    prestamos.prestar.addActionListener(
        e ->
            validar(
                () -> {
                  int est = comboId(prestamos.estudiante), libro = comboId(prestamos.libro);
                  ejecutar(
                      () -> prestamoService.prestar(est, libro),
                      p -> {
                        mensaje(
                            "Préstamo registrado. Vencimiento: " + p.getFechaDevolucion(), false);
                        refrescar();
                      });
                }));
    prestamos.devolver.addActionListener(
        e ->
            validar(
                () -> {
                  int id = seleccionado(prestamos.idSeleccionado());
                  ejecutar(
                      () -> prestamoService.devolver(id),
                      p -> {
                        mensaje(
                            "Devolución registrada. Días de atraso: " + p.getDiasAtraso(), false);
                        refrescar();
                      });
                }));
    prestamos.eliminar.addActionListener(
        e ->
            validar(
                () -> {
                  int id = seleccionado(prestamos.idSeleccionado());
                  if (confirmar(
                      "¿Eliminar definitivamente este registro del historial? Si sigue pendiente,"
                          + " se repondrá su ejemplar. Usa esta opción solo para corregir un"
                          + " registro erróneo."))
                    ejecutar(
                        () -> {
                          prestamoService.eliminar(id);
                          return null;
                        },
                        x -> refrescar());
                }));
    reportes.consultar.addActionListener(e -> validar(this::reporte));
    view.salir.addActionListener(
        e -> {
          view.dispose();
          new LoginController();
        });
  }

  private record Datos(
      List<Libro> libros,
      List<Categoria> categorias,
      List<Estudiante> estudiantes,
      List<Usuario> usuarios,
      List<Prestamo> prestamos) {}

  private void refrescar() {
    ejecutar(
        () ->
            new Datos(
                service.libros(),
                service.categorias(),
                service.estudiantes(),
                usuario.puedeGestionar() ? service.usuarios() : List.of(),
                prestamoService.listar()),
        d -> {
          listaLibros = d.libros();
          listaCategorias = d.categorias();
          listaEstudiantes = d.estudiantes();
          listaUsuarios = d.usuarios();
          libros.filas(
              listaLibros.stream()
                  .map(
                      l ->
                          new Object[] {
                            l.getId(),
                            l.getTitulo(),
                            l.getAutor(),
                            l.getIsbn(),
                            l.getEditorial(),
                            l.getStock(),
                            nombreCategoria(l.getIdCategoria())
                          })
                  .toList());
          categorias.filas(
              listaCategorias.stream().map(c -> new Object[] {c.getId(), c.getNombre()}).toList());
          estudiantes.filas(
              listaEstudiantes.stream()
                  .map(
                      s ->
                          new Object[] {
                            s.getId(), s.getNombre(), s.getRut(), s.getCurso(), s.getCorreo()
                          })
                  .toList());
          usuarios.filas(
              listaUsuarios.stream()
                  .map(
                      u ->
                          new Object[] {
                            u.getId(), u.getNombre(), u.getRut(), u.getCorreo(), u.getRol()
                          })
                  .toList());
          EditorPanel.opciones(categoria, listaCategorias);
          EditorPanel.opciones(prestamos.estudiante, listaEstudiantes);
          EditorPanel.opciones(prestamos.libro, listaLibros);
          EditorPanel.opciones(reportes.estudiante, listaEstudiantes);
          prestamos.filas(
              d.prestamos().stream()
                  .map(
                      p ->
                          new Object[] {
                            p.getId(),
                            nombreEstudiante(p.getIdEstudiante()),
                            nombreLibro(p.getIdLibro()),
                            p.getFechaPrestamo(),
                            p.getFechaDevolucion(),
                            p.getDevuelto() ? "Devuelto" : "Pendiente",
                            p.getFechaDevolucionReal(),
                            p.getDevuelto()
                                ? (p.getFechaDevolucionReal() == null
                                    ? "Sin registro"
                                    : p.getDiasAtraso())
                                : Math.max(
                                    0,
                                    ChronoUnit.DAYS.between(
                                        p.getFechaDevolucion(), LocalDate.now()))
                          })
                  .toList());
          
          libros.limpiar();
          categorias.limpiar();
          estudiantes.limpiar();
          usuarios.limpiar();
        });
  }

  private String nombreCategoria(int id) {
    return listaCategorias.stream()
        .filter(x -> x.getId() == id)
        .map(Categoria::getNombre)
        .findFirst()
        .orElse("#" + id);
  }

  private String nombreLibro(int id) {
    return listaLibros.stream()
        .filter(x -> x.getId() == id)
        .map(Libro::getTitulo)
        .findFirst()
        .orElse("#" + id);
  }

  private String nombreEstudiante(int id) {
    return listaEstudiantes.stream()
        .filter(x -> x.getId() == id)
        .map(Estudiante::getNombre)
        .findFirst()
        .orElse("#" + id);
  }

  private void seleccionLibro() {
    listaLibros.stream()
        .filter(x -> x.getId() == libros.idSeleccionado())
        .findFirst()
        .ifPresent(
            l -> {
              titulo.setText(l.getTitulo());
              autor.setText(l.getAutor());
              isbn.setText(l.getIsbn());
              editorial.setText(l.getEditorial());
              stock.setText("" + l.getStock());
              EditorPanel.seleccionar(categoria, l.getIdCategoria());
            });
  }

  private void seleccionCategoria() {
    listaCategorias.stream()
        .filter(x -> x.getId() == categorias.idSeleccionado())
        .findFirst()
        .ifPresent(c -> catNombre.setText(c.getNombre()));
  }

  private void seleccionEstudiante() {
    listaEstudiantes.stream()
        .filter(x -> x.getId() == estudiantes.idSeleccionado())
        .findFirst()
        .ifPresent(
            s -> {
              estNombre.setText(s.getNombre());
              estRut.setText(s.getRut());
              curso.setText(s.getCurso());
              estCorreo.setText(s.getCorreo());
              estClave.setText("");
            });
  }

  private void seleccionUsuario() {
    listaUsuarios.stream()
        .filter(x -> x.getId() == usuarios.idSeleccionado())
        .findFirst()
        .ifPresent(
            u -> {
              usrNombre.setText(u.getNombre());
              usrRut.setText(u.getRut());
              usrCorreo.setText(u.getCorreo());
              rol.setSelectedItem(u.getRol());
              usrClave.setText("");
            });
  }

  private record Reporte(String[] headers, List<Object[]> rows) {}

  private void reporte() {
    String tipo = (String) reportes.tipo.getSelectedItem();
    int est = "Historial por estudiante".equals(tipo) ? comboId(reportes.estudiante) : 0;
    ejecutar(
        () -> {
          ReporteDAO dao = new ReporteDAO();
          if ("Libros más prestados".equals(tipo)) {
            service.exigirAdmin();
            return new Reporte(
                new String[] {"Título", "Autor", "Total préstamos"}, dao.masPrestados());
          }
          if ("Historial por estudiante".equals(tipo)) {
            service.autorizarEstudiante(est);
            return new Reporte(
                new String[] {
                  "ID",
                  "Estudiante",
                  "Libro",
                  "Préstamo",
                  "Vencimiento",
                  "Estado",
                  "Devolución real",
                  "Días atraso"
                },
                dao.historial(est));
          }
          return new Reporte(
              new String[] {"ID", "Estudiante", "Libro", "Préstamo", "Vencimiento", "Días atraso"},
              dao.activos(usuario.puedeGestionar() ? null : service.estudiantePropio()));
        },
        r -> reportes.filas(r.headers(), r.rows()));
  }
}
