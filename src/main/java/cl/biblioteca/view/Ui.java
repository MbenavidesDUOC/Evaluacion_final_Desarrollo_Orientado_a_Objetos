package cl.biblioteca.view;

import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.*;

public final class Ui {
  public static final Color NAVY = new Color(36, 55, 78),
      GREEN = new Color(37, 116, 105),
      BG = new Color(244, 247, 249);

  private Ui() {}

  public static void estilo() {
    UIManager.put("Label.font", new Font("SansSerif", Font.PLAIN, 14));
    UIManager.put("Button.font", new Font("SansSerif", Font.PLAIN, 14));
    UIManager.put("Table.font", new Font("SansSerif", Font.PLAIN, 13));
    UIManager.put("TableHeader.font", new Font("SansSerif", Font.BOLD, 13));
    UIManager.put("TextField.font", new Font("SansSerif", Font.PLAIN, 14));
    UIManager.put("TabbedPane.font", new Font("SansSerif", Font.BOLD, 14));
  }

  public static JButton boton(String text) {
    JButton b = new JButton(text);
    b.setFocusPainted(false);
    b.setBackground(GREEN);
    b.setForeground(Color.WHITE);
    b.setMargin(new Insets(8, 14, 8, 14));
    return b;
  }

  public static JLabel titulo(String text) {
    JLabel l = new JLabel(text);
    l.setFont(new Font("SansSerif", Font.BOLD, 22));
    l.setForeground(NAVY);
    return l;
  }

  public static void tabla(JTable t) {
    t.setRowHeight(29);
    t.setAutoCreateRowSorter(true);
    t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    t.getTableHeader().setReorderingAllowed(false);
    t.setGridColor(new Color(222, 229, 234));
    t.setFillsViewportHeight(true);
    t.addMouseMotionListener(
        new java.awt.event.MouseMotionAdapter() {
          @Override
          public void mouseMoved(java.awt.event.MouseEvent e) {
            int row = t.rowAtPoint(e.getPoint()), col = t.columnAtPoint(e.getPoint());
            Object value = (row < 0 || col < 0) ? null : t.getValueAt(row, col);
            t.setToolTipText(value == null ? null : value.toString());
          }
        });
  }

  public static void anchos(JTable t, int... widths) {
    for (int i = 0; i < widths.length && i < t.getColumnCount(); i++) {
      t.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
    }
  }

  public static String error(Throwable ex) {
    if (ex instanceof SQLException s) {
      String state = s.getSQLState();
      if ("23000".equals(state))
        return "No se pudo guardar o eliminar: hay un dato duplicado o registros relacionados."
            + " Revisa RUT, ISBN y préstamos asociados.";
      if (state != null && state.startsWith("08"))
        return "No se pudo conectar a MySQL. Revisa que esté iniciado y verifica db.properties.";
      if (s.getMessage() != null && s.getMessage().startsWith("Falta db.properties"))
        return s.getMessage();
      if ("42S22".equals(state))
        return "Faltan columnas de devolución. Ejecuta sql/03_registro_devoluciones.sql.";
      if ("28000".equals(state))
        return "MySQL rechazó las credenciales. Revisa usuario y contraseña en db.properties.";
      return "La base de datos no pudo completar la operación. Revisa los scripts y la"
          + " configuración. Detalle técnico disponible en la consola.";
    }
    return ex.getMessage() == null ? "No se pudo completar la operación." : ex.getMessage();
  }
}
