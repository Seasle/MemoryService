package org.seasle;

import java.util.Date;
import java.text.SimpleDateFormat;

public class DateFormatter {
    private static final DateFormatter dateFormatter = new DateFormatter();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    public String format(Date date) {
        return dateFormat.format(date);
    }

    public static DateFormatter getInstance() {
        return dateFormatter;
    }
}