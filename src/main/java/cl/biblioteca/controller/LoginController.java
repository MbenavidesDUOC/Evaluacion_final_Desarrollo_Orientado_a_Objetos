package cl.biblioteca.controller;

import cl.biblioteca.model.Usuario;
import cl.biblioteca.service.AuthService;
import cl.biblioteca.view.*;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

public final class LoginController {
  private final LoginView view = new LoginView();

  public LoginController() {
    view.ingresar.addActionListener(e -> ingresar());
    view.setVisible(true);
  }

  private void ingresar() {
    String id = view.identificador.getText().trim();
    String clave = new String(view.clave.getPassword());
    if (id.isEmpty() || clave.isEmpty()) {
      JOptionPane.showMessageDialog(view, "Completa el usuario y la contraseña.");
      return;
    }
    view.ocupado(true);
    new SwingWorker<Usuario, Void>() {
      protected Usuario doInBackground() throws Exception {
        return new AuthService().ingresar(id, clave);
      }

      protected void done() {
        try {
          Usuario u = get();
          view.dispose();
          new MainController(u);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
          Throwable cause = e.getCause();
          cause.printStackTrace();
          JOptionPane.showMessageDialog(
              view, Ui.error(cause), "No se pudo ingresar", JOptionPane.ERROR_MESSAGE);
        } finally {
          view.ocupado(false);
          view.clave.setText("");
        }
      }
    }.execute();
  }
}
