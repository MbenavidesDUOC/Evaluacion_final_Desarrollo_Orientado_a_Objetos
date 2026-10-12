package cl.biblioteca.model;

public final class Estudiante extends Persona {
  private String curso;

  public String getCurso() {
    return curso;
  }

  public void setCurso(String curso) {
    this.curso = curso;
  }

  @Override
  public String descripcion() {
    return getNombre() + " · " + curso;
  }
}
