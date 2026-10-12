package cl.biblioteca.view;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public final class ReportePanel extends JPanel {
  public final JComboBox<String> tipo =
      new JComboBox<>(
          new String[] {
            "Libros más prestados", "Historial por estudiante", "Libros actualmente en préstamo"
          });
  public final JComboBox<Object> estudiante = new JComboBox<>();
  public final JButton consultar = Ui.boton("Consultar");
  public final JTable tabla = new JTable();

  public ReportePanel(boolean admin) {
    super(new BorderLayout(12, 12));
    setBorder(new EmptyBorder(16, 18, 16, 18));
    setBackground(Ui.BG);
    JPanel north = new JPanel(new BorderLayout(10, 10));
    north.setOpaque(false);
    north.add(Ui.titulo("Reportes"), BorderLayout.NORTH);
    JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
    filtros.setOpaque(false);
    filtros.add(tipo);
    filtros.add(new JLabel("Estudiante para historial"));
    filtros.add(estudiante);
    filtros.add(consultar);
    north.add(filtros, BorderLayout.SOUTH);
    add(north, BorderLayout.NORTH);
    Ui.tabla(tabla);
    add(new JScrollPane(tabla), BorderLayout.CENTER);
    if (!admin) tipo.removeItemAt(0);
  }

  public void filas(String[] headers, List<Object[]> rows) {
    DefaultTableModel m =
        new DefaultTableModel(headers, 0) {
          public boolean isCellEditable(int r, int c) {
            return false;
          }
        };
    for (Object[] r : rows) m.addRow(r);
    tabla.setModel(m);
    if (headers.length == 3) Ui.anchos(tabla, 570, 300, 150);
    else if (headers.length == 8) Ui.anchos(tabla, 40, 170, 310, 105, 105, 90, 110, 90);
    else Ui.anchos(tabla, 40, 170, 400, 130, 130, 100);
  }

  public void ocupado(boolean busy) {
    tipo.setEnabled(!busy);
    estudiante.setEnabled(!busy);
    consultar.setEnabled(!busy);
  }
}
