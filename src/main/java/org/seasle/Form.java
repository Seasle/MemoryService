package org.seasle;

import com.github.lgooddatepicker.components.DatePicker;

import java.awt.Color;
import javax.swing.*;
import javax.swing.border.MatteBorder;

public class Form {
    public JPanel contentPane;
    public JPanel container;
    public JComboBox comboBox;
    public DatePicker fromDatePicker;
    public DatePicker toDatePicker;
    private JPanel statusBar;
    public JLabel status;

    public Form() {
        MatteBorder border = new MatteBorder(1, 0, 0, 0, new Color(17, 17, 17, 128));

        statusBar.setBorder(border);
    }
}