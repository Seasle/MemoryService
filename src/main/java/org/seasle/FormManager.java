package org.seasle;

import javax.swing.*;
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
    private final String title = "Объем дискового пространства";

    private final Logger logger = LoggerProvider.getInstance();
    private final Formatter formatter = Formatter.getInstance();

    private final Form form = new Form();
    private final Diagram diagram = new Diagram();

    private final List<Long> totalList = new ArrayList<>();
    private final List<Long> usedList = new ArrayList<>();
    private final List<Object> timeLabels = new ArrayList<>();
    private final List<Object> dateLabels = new ArrayList<>();
    private final List<Object> points = new ArrayList<>();

    private final double denominator = Math.pow(1024, 3);
    private JFrame frame = null;
    private Database database = null;
    private Set<String> disks = null;
    private boolean canUpdate = false;
    private long maxBytes = 0;
    // endregion

    // region Constructor
    public FormManager() {
        initInterface();
    }
    // endregion

    // region Private methods
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

        diagram.panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent event) {
                super.mouseExited(event);

                form.dateLabel.setText("Наведите на график");
                form.usedLabel.setText("Наведите на график");
            }
        });

        diagram.panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent event) {
                super.mouseMoved(event);

                try {
                    int index = diagram.getPointIndex();
                    if (index >= 0 && index < points.size()) {
                        Object point = usedList.get(index);

                        form.dateLabel.setText(timeLabels.get(index).toString());
                        form.usedLabel.setText(String.format("Использовано: %.2f GB", (long) point / denominator));
                    } else {
                        form.dateLabel.setText("Наведите на график");
                        form.usedLabel.setText("Наведите на график");
                    }
                } catch (Exception exception) {
                    logger.log(Level.WARNING, exception.getMessage());
                }
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
        canUpdate = false;

        int selectedIndex = form.comboBox.getSelectedIndex();
        this.disks = disks;

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
        try {
            ResultSet resultSet = database.getDisks();
            Set<String> disks = new HashSet<>();

            while (resultSet.next()) {
                disks.add(resultSet.getString("name"));
            }

            setDisks(disks);
        } catch (SQLException exception) {
            logger.log(Level.SEVERE, exception.getMessage());
        }

        LocalDate today = LocalDate.now();
        if (form.fromDatePicker.getDate() == null) {
            canUpdate = false;

            form.fromDatePicker.setDate(today.withDayOfMonth(1));
        }
        if (form.toDatePicker.getDate() == null) {
            canUpdate = false;

            form.toDatePicker.setDate(YearMonth.from(today).atEndOfMonth());
        }

        frame.setVisible(true);

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
                totalList.clear();
                usedList.clear();
                timeLabels.clear();
                dateLabels.clear();
                points.clear();

                while (resultSet.next()) {
                    long total = resultSet.getLong("total");
                    long usable = resultSet.getLong("usable");
                    Date date = new Date(resultSet.getLong("timestamp") * 1000);

                    totalList.add(total);
                    usedList.add(total - usable);
                    timeLabels.add(formatter.formatTime(date));
                    dateLabels.add(formatter.formatDate(date));
                }

                if (totalList.size() > 0) {
                    int count = totalList.size();
                    maxBytes = Collections.max(totalList);

                    for (int index = 0; index < count; index++) {
                        points.add((double) usedList.get(index) / maxBytes);
                    }

                    diagram.setLabels(dateLabels);
                    diagram.setPoints(points);
                    diagram.draw();
                } else {
                    diagram.clear();
                }
            } catch (SQLException exception) {
                logger.log(Level.SEVERE, exception.getMessage());
            }

            form.totalLabel.setText(String.format("Объем: %.0f GB", Math.floor(maxBytes / denominator)));
            form.dateLabel.setText("Наведите на график");
            form.usedLabel.setText("Наведите на график");
        }
    }

    public boolean isVisible() {
        return frame.isVisible();
    }

    public String getTitle() {
        return title;
    }
    // endregion

    public static FormManager getInstance() {
        return formManager;
    }
}