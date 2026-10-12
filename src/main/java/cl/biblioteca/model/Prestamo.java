package cl.biblioteca.model;

public final class Prestamo implements Entidad {
  private int id;
  private int idEstudiante;
  private int idLibro;
  private java.time.LocalDate fechaPrestamo;
  private java.time.LocalDate fechaDevolucion;
  private boolean devuelto;
  private java.time.LocalDate fechaDevolucionReal;
  private int diasAtraso;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getIdEstudiante() {
    return idEstudiante;
  }

  public void setIdEstudiante(int idEstudiante) {
    this.idEstudiante = idEstudiante;
  }

  public int getIdLibro() {
    return idLibro;
  }

  public void setIdLibro(int idLibro) {
    this.idLibro = idLibro;
  }

  public java.time.LocalDate getFechaPrestamo() {
    return fechaPrestamo;
  }

  public void setFechaPrestamo(java.time.LocalDate fechaPrestamo) {
    this.fechaPrestamo = fechaPrestamo;
  }

  public java.time.LocalDate getFechaDevolucion() {
    return fechaDevolucion;
  }

  public void setFechaDevolucion(java.time.LocalDate fechaDevolucion) {
    this.fechaDevolucion = fechaDevolucion;
  }

  public boolean getDevuelto() {
    return devuelto;
  }

  public void setDevuelto(boolean devuelto) {
    this.devuelto = devuelto;
  }

  public java.time.LocalDate getFechaDevolucionReal() {
    return fechaDevolucionReal;
  }

  public void setFechaDevolucionReal(java.time.LocalDate fechaDevolucionReal) {
    this.fechaDevolucionReal = fechaDevolucionReal;
  }

  public int getDiasAtraso() {
    return diasAtraso;
  }

  public void setDiasAtraso(int diasAtraso) {
    this.diasAtraso = diasAtraso;
  }
}
