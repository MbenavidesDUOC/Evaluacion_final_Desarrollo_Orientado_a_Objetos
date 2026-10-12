package cl.biblioteca.view;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public final class EditorPanel extends JPanel {
  public final JTable tabla = new JTable();
  public final JButton guardar = Ui.boton("Guardar"),
      eliminar = Ui.boton("Eliminar"),
      nuevo = Ui.boton("Nuevo"),
      actualizar = Ui.boton("Actualizar");
  private final Map<String, JComponent> campos = new LinkedHashMap<>();
  private final JPanel formulario = new JPanel(new GridBagLayout());
  private final String[] columnas;

  public EditorPanel(String titulo, String[] columnas, boolean editable) {
    super(new BorderLayout(12, 12));
    this.columnas = columnas;
    setBorder(new EmptyBorder(16, 18, 16, 18));
    setBackground(Ui.BG);
    add(Ui.titulo(titulo), BorderLayout.NORTH);
    Ui.tabla(tabla);
    add(new JScrollPane(tabla), BorderLayout.CENTER);
    JPanel sur = new JPanel(new BorderLayout(8, 8));
    sur.setOpaque(false);
    formulario.setOpaque(false);
    if (editable) sur.add(formulario, BorderLayout.CENTER);
    JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
    acciones.setOpaque(false);
    if (editable) {
      acciones.add(nuevo);
      acciones.add(guardar);
      acciones.add(eliminar);
    }
    acciones.add(actualizar);
    sur.add(acciones, BorderLayout.SOUTH);
    add(sur, BorderLayout.SOUTH);
    filas(List.of());
  }

  public JTextField texto(String nombre) {
    JTextField f = new JTextField(17);
    campo(nombre, f);
    return f;
  }

  public JPasswordField password(String nombre) {
    JPasswordField f = new JPasswordField(17);
    campo(nombre, f);
    return f;
  }

  public JComboBox<Object> combo(String nombre) {
    JComboBox<Object> c = new JComboBox<>();
    campo(nombre, c);
    return c;
  }

  private void campo(String nombre, JComponent component) {
    int i = campos.size();
    campos.put(nombre, component);
    GridBagConstraints g = new GridBagConstraints();
    g.insets = new Insets(4, 6, 4, 6);
    g.anchor = GridBagConstraints.WEST;
    g.gridx = (i % 3) * 2;
    g.gridy = i / 3;
    formulario.add(new JLabel(nombre), g);
    g.gridx++;
    g.fill = GridBagConstraints.HORIZONTAL;
    g.weightx = 1;
    formulario.add(component, g);
  }

  public void filas(List<Object[]> rows) {
    DefaultTableModel m =
        new DefaultTableModel(columnas, 0) {
          public boolean isCellEditable(int r, int c) {
            return false;
          }
        };
    for (Object[] r : rows) m.addRow(r);
    tabla.setModel(m);
    if (columnas.length == 7) Ui.anchos(tabla, 40, 380, 175, 145, 135, 110, 140);
    else if (columnas.length == 5) Ui.anchos(tabla, 40, 250, 130, 200, 280);
    else Ui.anchos(tabla, 50, 500);
  }

  public int idSeleccionado() {
    int row = tabla.getSelectedRow();
    return row < 0 ? 0 : ((Number) tabla.getValueAt(row, 0)).intValue();
  }

  public void limpiar() {
    tabla.clearSelection();
    for (JComponent c : campos.values()) {
      if (c instanceof JTextField f) f.setText("");
      if (c instanceof JComboBox<?> box && box.getItemCount() > 0) box.setSelectedIndex(0);
    }
  }

  public void ocupado(boolean busy) {
    guardar.setEnabled(!busy);
    eliminar.setEnabled(!busy);
    nuevo.setEnabled(!busy);
    actualizar.setEnabled(!busy);
    tabla.setEnabled(!busy);
    for (JComponent c : campos.values()) c.setEnabled(!busy);
  }

  public static void opciones(JComboBox<Object> box, List<?> values) {
    Object selected = box.getSelectedItem();
    box.removeAllItems();
    for (Object v : values) box.addItem(v);
    if (selected instanceof cl.biblioteca.model.Entidad e) seleccionar(box, e.getId());
  }

  public static void seleccionar(JComboBox<Object> box, int id) {
    for (int i = 0; i < box.getItemCount(); i++)
      if (box.getItemAt(i) instanceof cl.biblioteca.model.Entidad e && e.getId() == id) {
        box.setSelectedIndex(i);
        return;
      }
  }
}
