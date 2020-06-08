package org.seasle;

import java.util.Date;
import java.text.SimpleDateFormat;

public class Formatter {
    private static final Formatter dateFormatter = new Formatter();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    public String formatTime(Date date) { return timeFormat.format(date); }

    public String formatDate(Date date) {
        return dateFormat.format(date);
    }

    public static Formatter getInstance() {
        return dateFormatter;
    }
}