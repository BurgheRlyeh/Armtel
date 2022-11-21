package com.github.burgherlyeh.view;

import com.github.burgherlyeh.model.Model;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.event.*;

public class TableView implements ITableView {
    private final Model model;

    DefaultTableModel defaultTableModel;

    int selectedRow = -1;

    public TableView(Model model) {
        this.model = model;

        var frame = new JFrame("Multicast Traffic Receiver");

        // create and set menu bar
        var menuBar = new JMenuBar();
        menuBar.add(createActionMenu());
        frame.setJMenuBar(menuBar);

        // create and add scrollable table
        var columnNames = new String[]{
                "Unique ID",
                "MAC address",
                "Source IP",
                "Device type",
                "SIP",
                "Source port",
                "Age"
        };
        defaultTableModel = new DefaultTableModel(new Object[][]{}, columnNames);

        var table = createTable(defaultTableModel);
        frame.add(new JScrollPane(table));

        frame.setSize(1280, 720);                           // window size
        frame.setVisible(true);                                         // window visible on
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);  // auto close application
    }

    private JMenu createActionMenu() {
        var actionMenu = new JMenu("Action");

        var isOffline = new JCheckBoxMenuItem("Offline Mode");
        isOffline.setState(false);
        // TODO Change table color in offline mode
        isOffline.addActionListener(e -> model.switchOfflineMode());

        actionMenu.add(isOffline);
        return actionMenu;
    }

    private JTable createTable(TableModel tableModel) {
        var table = new JTable(tableModel);

        table.setComponentPopupMenu(createPopupMenu());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectedRow = table.rowAtPoint(e.getPoint());
                System.out.println("selectedRow = " + selectedRow);
            }
        });

        return table;
    }

    private JPopupMenu createPopupMenu() {
        var popupMenu = new JPopupMenu();

        // TODO Replace checkbox with buttons
        var isDeviceOffline = new JCheckBoxMenuItem("Device Offline Mode");
        isDeviceOffline.setState(false);
        popupMenu.add(isDeviceOffline);
        // TODO Change row color of offline devices
        isDeviceOffline.addActionListener(l ->
                model.switchDeviceOfflineMode(
                        (String) defaultTableModel
                                .getDataVector()
                                .get(selectedRow)
                                .get(0)
                ));

        return popupMenu;
    }

    @Override
    public void updateTableView() {
        var data = model.getTableData();
        updateColumn(data, defaultTableModel.getColumnCount() - 1);
        addNewRows(data);
    }

    private void updateColumn(Object[][] data, int column) {
        for (var i = 0; i < defaultTableModel.getRowCount(); ++i) {
            defaultTableModel.setValueAt(data[i][column], i, column);
            defaultTableModel.fireTableCellUpdated(i, column);
        }
    }

    private void addNewRows(Object[][] data) {
        for (var i = defaultTableModel.getRowCount(); i < data.length; ++i) {
            defaultTableModel.addRow(data[i]);
        }
        defaultTableModel.fireTableDataChanged();
    }
}
