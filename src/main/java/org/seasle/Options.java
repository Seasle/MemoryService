package org.seasle;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;

public class Options {
    public JPanel contentPane;
    public JSlider intervalSlider;
    public JSlider thresholdSlider;
    private JLabel intervalValue;
    private JLabel thresholdValue;

    private final List<String> labels = new ArrayList<String>(){{
        add("5 минут");
        add("10 минут");
        add("15 минут");
        add("20 минут");
        add("30 минут");
        add("1 час");
        add("2 часа");
        add("4 часа");
    }};
    private final List<Integer> intervals = new ArrayList<Integer>(){{
        add(5 * 60 * 1000);
        add(10 * 60 * 1000);
        add(15 * 60 * 1000);
        add(20 * 60 * 1000);
        add(30 * 60 * 1000);
        add(60 * 60 * 1000);
        add(120 * 60 * 1000);
        add(240 * 60 * 1000);
    }};

    public Options() {
        intervalSlider.addChangeListener(event -> updateIntervalValue());
        thresholdSlider.addChangeListener(event -> updateThresholdValue());

        updateIntervalValue();
        updateThresholdValue();
    }

    private void updateIntervalValue() {
        intervalValue.setText(labels.get(intervalSlider.getValue()));
    }

    private void updateThresholdValue() {
        thresholdValue.setText(String.valueOf(thresholdSlider.getValue()) + "%");
    }

    public int getIntervalValue() {
        return intervals.get(intervalSlider.getValue());
    }

    public void setIntervalValue(Object value) {
        int key = 0;

        int count = intervals.size();
        for (int index = 0; index < count; index++) {
            int number = intervals.get(index);

            if (number == (int) value) {
                key = index;

                break;
            }
        }

        intervalSlider.setValue(key);
    }

    public double getThresholdValue() {
        return thresholdSlider.getValue() / 100.0;
    }

    public void setThresholdValue(Object value) {
        double threshold = 0.0;
        try {
            threshold = Double.parseDouble(value.toString());
        } catch (Exception ignored) {};

        thresholdSlider.setValue((int) (threshold * 100));
    }
}