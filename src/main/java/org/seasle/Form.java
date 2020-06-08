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
    private JPanel col_1;
    private JPanel col_2;
    private JPanel col_3;
    public JLabel totalLabel;
    public JLabel dateLabel;
    public JLabel usedLabel;

    public Form() {
        MatteBorder topBorder = new MatteBorder(1, 0, 0, 0, new Color(17, 17, 17, 128));
        MatteBorder rightBorder = new MatteBorder(0, 0, 0, 1, new Color(17, 17, 17, 128));

        statusBar.setBorder(topBorder);
        col_1.setBorder(rightBorder);
        col_2.setBorder(rightBorder);
        col_3.setBorder(rightBorder);
    }
}