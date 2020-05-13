package org.seasle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

public class FormManager {
    private static final FormManager formManager = new FormManager();

    private final Logger logger = LoggerProvider.getInstance();
    private final String title = "Интерфейс";
    private final Form form = new Form();
    private JFrame frame = null;
    private Database database = null;

    public FormManager() {
        this.initTray();
        this.initInterface();
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    private void initTray() {
        SystemTray tray = SystemTray.getSystemTray();
        try {
            ImageIcon icon = new ImageIcon(
                getClass().getResource("/icon_16.png")
            );
            TrayIcon trayIcon = new TrayIcon(icon.getImage());

            PopupMenu popupMenu = new PopupMenu();
            MenuItem openItem = new MenuItem("Открыть");
            MenuItem exitItem = new MenuItem("Выход");

            openItem.addActionListener(event -> {
                this.showInterface();
            });

            exitItem.addActionListener(event -> {
                System.exit(0);
            });

            popupMenu.add(openItem);
            popupMenu.add(exitItem);

            trayIcon.setToolTip(this.title);
            trayIcon.setPopupMenu(popupMenu);

            tray.add(trayIcon);

            logger.log(Level.INFO, "Tray icon has been successfully created.");
        } catch (AWTException exception) {
            logger.log(Level.WARNING, exception.getMessage());
        }
    }

    private void initInterface() {
        ImageIcon icon = new ImageIcon(
            getClass().getResource("/icon_32.png")
        );

        this.frame = new JFrame(this.title);

        this.frame.setIconImage(icon.getImage());
        this.frame.setContentPane(form.contentPane);
        this.frame.setSize(600, 400);
        this.frame.setLocationRelativeTo(null);
        this.frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                super.windowClosing(event);

                logger.log(Level.INFO, "Interface has been closed.");
            }
        });

        logger.log(Level.INFO, "Interface has been successfully created.");
    }

    private void updateInterface() {
        ResultSet resultSet = this.database.getAllData();
        List<Object[]> data = new ArrayList<Object[]>();

        try {
            while(resultSet.next()) {
                long total = resultSet.getLong("total");
                long usable = resultSet.getLong("usable");

                Object[] entry = {
                    resultSet.getString("name"),
                    total,
                    total - usable,
                    usable
                };

                data.add(entry);
            }

            form.fillTable(data);
        } catch (SQLException exception) {
            logger.log(Level.SEVERE, exception.getMessage());
        }
    }

    public void showInterface() {
        this.updateInterface();
        this.frame.setVisible(true);

        logger.log(Level.INFO, "Interface has been opened.");
    }

    public static FormManager getInstance() {
        return formManager;
    }
}