package org.seasle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.atomic.AtomicInteger;

public class FormManager {
    private static final FormManager formManager = new FormManager();

    // region Variables
    private final Logger logger = LoggerProvider.getInstance();
    private final DateFormatter dateFormatter = DateFormatter.getInstance();
    private final String title = "Объем дискового пространства";
    private final Form form = new Form();
    private final Diagram diagram = new Diagram();
    private JFrame frame = null;
    private Database database = null;
    private Set<String> disks = null;
    private boolean canUpdate = false;
    // endregion

    // region Constructor
    public FormManager() {
        initTray();
        initInterface();
    }
    // endregion

    // region Private methods
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
                showInterface();
            });

            exitItem.addActionListener(event -> {
                System.exit(0);
            });

            popupMenu.add(openItem);
            popupMenu.addSeparator();
            popupMenu.add(exitItem);

            trayIcon.setToolTip(title);
            trayIcon.setPopupMenu(popupMenu);

            tray.add(trayIcon);

            logger.log(Level.INFO, "Tray icon has been successfully created.");
        } catch (AWTException exception) {
            logger.log(Level.WARNING, exception.getMessage());
        }
    }

    private void initInterface() {
        AtomicInteger selectedIndex = new AtomicInteger(form.comboBox.getSelectedIndex());

        form.comboBox.addActionListener(event -> {
            int currentIndex = form.comboBox.getSelectedIndex();
            if (selectedIndex.get() != currentIndex) {
                updateInterface();
            }

            selectedIndex.set(currentIndex);
        });

        form.fromDatePicker.addDateChangeListener(event -> {
            updateInterface();
        });

        form.toDatePicker.addDateChangeListener(event -> {
            updateInterface();
        });

        ImageIcon icon = new ImageIcon(
            getClass().getResource("/icon_32.png")
        );

        frame = new JFrame(title);

        frame.setIconImage(icon.getImage());
        frame.setContentPane(form.contentPane);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                super.windowClosing(event);

                logger.log(Level.INFO, "Interface has been closed.");
            }
        });

        form.container.add(diagram.panel);

        logger.log(Level.INFO, "Interface has been successfully created.");
    }
    // endregion

    // region Public methods
    public void setDatabase(Database database) {
        this.database = database;
    }

    public void setDisks(Set<String> disks) {
        this.disks = disks;
        canUpdate = false;

        int selectedIndex = form.comboBox.getSelectedIndex();

        form.comboBox.removeAllItems();
        for (String disk : disks) {
            form.comboBox.addItem(String.format("Диск %s", disk));
        }
        if (selectedIndex >= 0) {
            form.comboBox.setSelectedIndex(selectedIndex);
        }

        canUpdate = true;
    }

    public void showInterface() {
        frame.setVisible(true);

        LocalDate today = LocalDate.now();
        if (form.fromDatePicker.getDate() == null) {
            canUpdate = false;

            form.fromDatePicker.setDate(today.withDayOfMonth(1));
        }
        if (form.toDatePicker.getDate() == null) {
            canUpdate = false;

            form.toDatePicker.setDate(YearMonth.from(today).atEndOfMonth());
        }

        if (!canUpdate) {
            canUpdate = true;

            updateInterface();
        }

        logger.log(Level.INFO, "Interface has been opened.");
    }

    public void updateInterface() {
        if (canUpdate) {
            LocalDate fromDate = form.fromDatePicker.getDate();
            LocalDate toDate = form.toDatePicker.getDate();
            LocalDateTime from = LocalDateTime.of(
                    fromDate != null ? fromDate : LocalDate.of(1970, 1, 1),
                    LocalTime.of(0, 0, 0)
            );
            LocalDateTime to = LocalDateTime.of(
                    toDate != null ? toDate : LocalDate.of(9999, 12, 31),
                    LocalTime.of(23, 59, 59)
            );
            ResultSet resultSet = database.getData(
                    disks.toArray()[form.comboBox.getSelectedIndex()].toString(),
                    from.atZone(ZoneId.systemDefault()).toEpochSecond(),
                    to.atZone(ZoneId.systemDefault()).toEpochSecond()
            );

            try {
                List<Long> totalList = new ArrayList<>();
                List<Long> usedList = new ArrayList<>();
                List<Object> marks = new ArrayList<>();
                List<Object> labels = new ArrayList<>();
                List<Object> points = new ArrayList<>();

                while (resultSet.next()) {
                    long total = resultSet.getLong("total");
                    long usable = resultSet.getLong("usable");

                    totalList.add(total);
                    usedList.add(total - usable);
                    labels.add(dateFormatter.format(new Date(resultSet.getLong("timestamp") * 1000)));
                }

                if (totalList.size() > 0) {
                    int count = totalList.size();
                    long maxBytes = Collections.max(totalList);
                    double denominator = Math.pow(1024, 3);

                    for (int index = 0 ; index <= 10; index++) {
                        long piece = maxBytes / 10 * index;

                        marks.add(String.format("%.0f GB", Math.floor(piece / denominator)));
                    }

                    for (int index = 0; index < count; index++) {
                        points.add((double) usedList.get(index) / maxBytes);
                    }

                    diagram.setMarks(marks);
                    diagram.setLabels(labels);
                    diagram.setPoints(points);
                    diagram.draw();
                } else {
                    diagram.clear();
                }

            } catch (SQLException exception) {
                logger.log(Level.SEVERE, exception.getMessage());
            }
        }
    }

    public boolean isVisible() {
        return frame.isVisible();
    }
    // endregion

    public static FormManager getInstance() {
        return formManager;
    }
}