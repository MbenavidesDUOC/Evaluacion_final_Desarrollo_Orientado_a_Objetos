package cl.biblioteca.model;

public abstract class Usuario extends Persona {
  private String clave;

  public String getClave() {
    return clave;
  }

  public void setClave(String clave) {
    this.clave = clave;
  }

  public abstract String getRol();

  public abstract boolean puedeGestionar();

  @Override
  public String descripcion() {
    return getNombre() + " · " + getRol();
  }
}
