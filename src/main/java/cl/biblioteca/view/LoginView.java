package cl.biblioteca.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public final class LoginView extends JFrame {
  public final JTextField identificador = new JTextField(24);
  public final JPasswordField clave = new JPasswordField(24);
  public final JButton ingresar = Ui.boton("Ingresar");
  private final JLabel estado = new JLabel("Ingresa con tu RUT o correo.");

  public LoginView() {
    super("Biblioteca escolar · Ingreso");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    JPanel p = new JPanel(new GridBagLayout());
    p.setBackground(Ui.BG);
    p.setBorder(new EmptyBorder(28, 36, 28, 36));
    GridBagConstraints g = new GridBagConstraints();
    g.gridx = 0;
    g.gridy = 0;
    g.fill = GridBagConstraints.HORIZONTAL;
    g.insets = new Insets(7, 0, 7, 0);
    p.add(Ui.titulo("Biblioteca escolar"), g);
    g.gridy++;
    p.add(new JLabel("Acceso de bibliotecarios y estudiantes"), g);
    g.gridy++;
    p.add(new JLabel("RUT o correo"), g);
    g.gridy++;
    p.add(identificador, g);
    g.gridy++;
    p.add(new JLabel("Contraseña"), g);
    g.gridy++;
    p.add(clave, g);
    g.gridy++;
    p.add(ingresar, g);
    g.gridy++;
    p.add(estado, g);
    setContentPane(p);
    getRootPane().setDefaultButton(ingresar);
    pack();
    setResizable(false);
    setLocationRelativeTo(null);
  }

  public void ocupado(boolean busy) {
    ingresar.setEnabled(!busy);
    identificador.setEnabled(!busy);
    clave.setEnabled(!busy);
    estado.setText(busy ? "Conectando…" : "Ingresa con tu RUT o correo.");
  }
}
