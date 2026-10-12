package cl.biblioteca.model;

public final class Bibliotecario extends Usuario {
  @Override
  public String getRol() {
    return "bibliotecario";
  }

  @Override
  public boolean puedeGestionar() {
    return true;
  }
}
