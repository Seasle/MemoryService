package org.seasle;

import javax.swing.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

public class OptionsManager {
    private static final OptionsManager optionsManager = new OptionsManager();

    // region Variables
    private final String title = "Настройки";

    private final Logger logger = LoggerProvider.getInstance();

    private final Options form = new Options();

    private JFrame frame = null;
    private Database database = null;
    private HashMap<String, Object> options = null;
    // endregion

    // region Constructor
    public OptionsManager() {
        initInterface();
    }
    // endregion

    // region Private methods
    private void initInterface() {
        ImageIcon icon = new ImageIcon(
            getClass().getResource("/icon_32.png")
        );

        frame = new JFrame(title);

        frame.setIconImage(icon.getImage());
        frame.setContentPane(form.contentPane);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                super.windowClosing(event);

                updateOptions();

                logger.log(Level.INFO, "Options interface has been closed.");
            }
        });

        form.intervalSlider.addChangeListener(event -> {
            database.saveOption("interval", form.getIntervalValue());
        });

        form.thresholdSlider.addChangeListener(event -> {
            database.saveOption("threshold", form.getThresholdValue());
        });

        logger.log(Level.INFO, "Options interface has been successfully created.");
    }

    private void setIntervalValue(Object value) {
        form.setIntervalValue(value);
    }

    private void setThresholdValue(Object value) {
        form.setThresholdValue(value);
    }

    private void updateOptions() {
        options.put("interval", (Object) form.getIntervalValue());
        options.put("threshold", (Object) form.getThresholdValue());
    }
    // endregion

    // region Public methods
    public void setDatabase(Database database) {
        this.database = database;
    }

    public void setOptions(HashMap<String, Object> options) {
        this.options = options;

        setIntervalValue(options.get("interval"));
        setThresholdValue(options.get("threshold"));
    }

    public void showInterface() {
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        logger.log(Level.INFO, "Options interface has been opened.");
    }
    // endregion

    public static OptionsManager getInstance() {
        return optionsManager;
    }
}