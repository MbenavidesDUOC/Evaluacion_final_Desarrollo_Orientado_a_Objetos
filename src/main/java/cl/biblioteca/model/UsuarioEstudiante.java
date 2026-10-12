package cl.biblioteca.model;

public final class UsuarioEstudiante extends Usuario {
  @Override
  public String getRol() {
    return "estudiante";
  }

  @Override
  public boolean puedeGestionar() {
    return false;
  }
}
