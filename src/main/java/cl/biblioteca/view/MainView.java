package cl.biblioteca.view;

import cl.biblioteca.model.Usuario;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public final class MainView extends JFrame {
  public final JTabbedPane tabs = new JTabbedPane();
  public final JButton salir = Ui.boton("Cerrar sesión");
  private final JLabel estado = new JLabel("Cargando datos…");
  private final java.util.List<EditorPanel> editores = new ArrayList<>();
  private PrestamoPanel prestamos;
  private ReportePanel reportes;

  public MainView(Usuario u) {
    super("Biblioteca escolar");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setSize(1180, 750);
    setMinimumSize(new Dimension(980, 620));
    setLocationRelativeTo(null);
    JPanel base = new JPanel(new BorderLayout(0, 8));
    base.setBackground(Ui.BG);
    JPanel cabecera = new JPanel(new BorderLayout());
    cabecera.setBackground(Ui.NAVY);
    cabecera.setBorder(new EmptyBorder(14, 18, 14, 18));
    JLabel title = new JLabel("Biblioteca escolar  |  " + u.descripcion());
    title.setForeground(Color.WHITE);
    title.setFont(new Font("SansSerif", Font.BOLD, 18));
    cabecera.add(title, BorderLayout.CENTER);
    cabecera.add(salir, BorderLayout.EAST);
    base.add(cabecera, BorderLayout.NORTH);
    base.add(tabs, BorderLayout.CENTER);
    estado.setBorder(new EmptyBorder(6, 18, 10, 18));
    base.add(estado, BorderLayout.SOUTH);
    setContentPane(base);
  }

  public void editor(String titulo, EditorPanel p) {
    editores.add(p);
    tabs.addTab(titulo, p);
  }

  public void prestamos(PrestamoPanel p) {
    prestamos = p;
    tabs.addTab("Préstamos", p);
  }

  public void reportes(ReportePanel p) {
    reportes = p;
    tabs.addTab("Reportes", p);
  }

  public void ocupado(boolean busy) {
    for (EditorPanel p : editores) p.ocupado(busy);
    if (prestamos != null) prestamos.ocupado(busy);
    if (reportes != null) reportes.ocupado(busy);
    salir.setEnabled(!busy);
    estado.setText(
        busy
            ? "Procesando en segundo plano…"
            : "Conexión activa · Selecciona una fila para modificar o devolver.");
  }
}
