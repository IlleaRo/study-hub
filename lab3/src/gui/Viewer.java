package gui;

import raster.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class Viewer extends JPanel {
    JTable currentTable = null;
    public Viewer(PNM pnm) throws ClassNotFoundException {
        super();
        update(pnm);
    }

    public void update(PNM pnm) throws ClassNotFoundException {
        int width = pnm.getWidth();
        int height = pnm.getHeight();

        JTable table = new JTable(new DefaultTableModel(height, width));
        setSize(width * 10, height * 10);
        table.setShowGrid(false);

        // Установка размеров ячеек
        table.setRowHeight(15);
        for (int col = 0; col < width; col++) {
            table.getColumnModel().getColumn(col).setMaxWidth(10);
        }

        // Отключение автоматического изменения размеров столбцов
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        if (pnm.getClass() == PBM.class) {
            int color;
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    color = pnm.getData()[row * width + col] == 1 ? 0 : 255;
                    table.setValueAt(new Color(color, color, color), row, col);
                }
            }
        } else if (pnm.getClass() == PGM.class) {
            int color;
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    color = (int) ((double)(pnm.getData()[row * width + col])/pnm.getMaxValue() * 255);
                    table.setValueAt(new Color(color, color, color), row, col);
                }
            }
        } else if (pnm.getClass() == PPM.class) {
            int red, green, blue;
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    red = (int) ((double)(pnm.getData()[row * width * pnm.getValuesPerPixel() + col * pnm.getValuesPerPixel()])/pnm.getMaxValue() * 255);
                    green = (int) ((double)(pnm.getData()[row * width * pnm.getValuesPerPixel()+ col * pnm.getValuesPerPixel() + 1])/pnm.getMaxValue() * 255);
                    blue = (int) ((double)(pnm.getData()[row * width * pnm.getValuesPerPixel() + col * pnm.getValuesPerPixel() + 2])/pnm.getMaxValue() * 255);
                    table.setValueAt(new Color(red, green, blue), row, col);
                }
            }
        } else {
            throw new ClassNotFoundException("Incorrect class!");
        }

        table.setDefaultRenderer(Object.class, new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel panel = new JPanel();
                panel.setBackground((Color) value);
                return panel;
            }
        });

        if (currentTable != null) {
            remove(currentTable);
        }

        currentTable = table;
        add(table, BorderLayout.CENTER);
        this.updateUI();
    }
}
