package cl.biblioteca.view;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public final class PrestamoPanel extends JPanel {
  public final JComboBox<Object> estudiante = new JComboBox<>(), libro = new JComboBox<>();
  public final JButton prestar = Ui.boton("Registrar préstamo"),
      devolver = Ui.boton("Devolver seleccionado"),
      eliminar = Ui.boton("Eliminar registro"),
      actualizar = Ui.boton("Actualizar");
  public final JTable tabla = new JTable();

  public PrestamoPanel(boolean admin) {
    super(new BorderLayout(12, 12));
    setBorder(new EmptyBorder(16, 18, 16, 18));
    setBackground(Ui.BG);
    JPanel top = new JPanel(new BorderLayout(8, 8));
    top.setOpaque(false);
    top.add(Ui.titulo("Préstamos y devoluciones"), BorderLayout.NORTH);
    JPanel fields = new JPanel(new GridLayout(2, 2, 12, 6));
    fields.setOpaque(false);
    fields.add(new JLabel("Estudiante"));
    fields.add(new JLabel("Libro"));
    fields.add(estudiante);
    fields.add(libro);
    top.add(fields, BorderLayout.CENTER);
    top.add(
        new JLabel("Plazo de préstamo: 7 días. El stock muestra ejemplares disponibles."),
        BorderLayout.SOUTH);
    add(top, BorderLayout.NORTH);
    Ui.tabla(tabla);
    add(new JScrollPane(tabla), BorderLayout.CENTER);
    JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
    acciones.setOpaque(false);
    acciones.add(prestar);
    acciones.add(devolver);
    if (admin) acciones.add(eliminar);
    acciones.add(actualizar);
    add(acciones, BorderLayout.SOUTH);
  }

  public void filas(List<Object[]> rows) {
    DefaultTableModel m =
        new DefaultTableModel(
            new String[] {
              "ID",
              "Estudiante",
              "Libro",
              "Préstamo",
              "Vencimiento",
              "Estado",
              "Devolución real",
              "Días atraso"
            },
            0) {
          public boolean isCellEditable(int r, int c) {
            return false;
          }
        };
    for (Object[] r : rows) m.addRow(r);
    tabla.setModel(m);
    Ui.anchos(tabla, 40, 170, 310, 105, 105, 90, 110, 90);
  }

  public int idSeleccionado() {
    int r = tabla.getSelectedRow();
    return r < 0 ? 0 : ((Number) tabla.getValueAt(r, 0)).intValue();
  }

  public void ocupado(boolean busy) {
    for (JButton b : new JButton[] {prestar, devolver, eliminar, actualizar}) b.setEnabled(!busy);
    estudiante.setEnabled(!busy);
    libro.setEnabled(!busy);
    tabla.setEnabled(!busy);
  }
}
