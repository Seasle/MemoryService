package org.seasle;

import javax.swing.*;
import javax.xml.crypto.Data;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GUI {
    private static final Logger logger = LoggerProvider.getInstance();
    private Database database = null;

    public final FormManager formManager = FormManager.getInstance();
    public final OptionsManager optionsManager = OptionsManager.getInstance();

    public GUI(Database database, HashMap<String, Object> options) {
        this.database = database;

        formManager.setDatabase(database);
        optionsManager.setDatabase(database);
        optionsManager.setOptions(options);

        initTray();
    }

    private void initTray() {
        SystemTray tray = SystemTray.getSystemTray();
        try {
            ImageIcon icon = new ImageIcon(
                GUI.class.getResource("/icon_16.png")
            );
            TrayIcon trayIcon = new TrayIcon(icon.getImage());

            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    super.mouseClicked(event);

                    if (SwingUtilities.isLeftMouseButton(event)) {
                        formManager.showInterface();
                    }
                }
            });

            PopupMenu popupMenu = new PopupMenu();
            MenuItem openItem = new MenuItem("Открыть");
            MenuItem optionsItem = new MenuItem("Настройки");
            MenuItem exitItem = new MenuItem("Выход");

            openItem.addActionListener(event -> {
                formManager.showInterface();
            });

            optionsItem.addActionListener(event -> {
                optionsManager.showInterface();
            });

            exitItem.addActionListener(event -> {
                System.exit(0);
            });

            popupMenu.add(openItem);
            popupMenu.add(optionsItem);
            popupMenu.addSeparator();
            popupMenu.add(exitItem);

            trayIcon.setToolTip(formManager.getTitle());
            trayIcon.setPopupMenu(popupMenu);

            tray.add(trayIcon);

            logger.log(Level.INFO, "Tray icon has been successfully created.");
        } catch (AWTException exception) {
            logger.log(Level.WARNING, exception.getMessage());
        }
    }
}