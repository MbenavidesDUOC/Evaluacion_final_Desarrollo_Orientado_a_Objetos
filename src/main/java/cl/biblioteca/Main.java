package cl.biblioteca;

import cl.biblioteca.config.DatabaseConnection;
import cl.biblioteca.controller.LoginController;
import cl.biblioteca.view.Ui;
import javax.swing.SwingUtilities;

public final class Main {
  private Main() {}

  public static void main(String[] args) {
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  try {
                    DatabaseConnection.getInstance().close();
                  } catch (Exception ignored) {
                  }
                }));
    SwingUtilities.invokeLater(
        () -> {
          Ui.estilo();
          new LoginController();
        });
  }
}
