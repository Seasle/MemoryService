package org.seasle;

import java.io.File;
import java.util.Date;
import java.util.logging.*;
import java.io.IOException;

public class ReportProvider {
    private static final ReportProvider reportProvider = new ReportProvider();
    private Logger logger = null;

    public ReportProvider() {
        File logsDirectory = new File("reports");
        if (!logsDirectory.exists()) {
            logsDirectory.mkdirs();
        }

        try {
            this.logger = Logger.getLogger("report");

            Formatter dateFormatter = Formatter.getInstance();
            String filename = String.format("reports/%s.log", dateFormatter.formatDate(new Date()));
            FileHandler fileHandler = new FileHandler(filename, true);
            SimpleFormatter simpleFormatter = new SimpleFormatter() {
                private static final String format = "[%1$tF %1$tT] %2$s%n";

                @Override
                public synchronized String format(LogRecord record) {
                    return String.format(
                        format,
                        new Date(record.getMillis()),
                        record.getMessage()
                    );
                }
            };

            fileHandler.setFormatter(simpleFormatter);
            logger.addHandler(fileHandler);
        } catch (IOException exception) {
            exception.printStackTrace();

            System.exit(1);
        }
    }

    public static Logger getInstance() {
        return reportProvider.logger;
    }
}