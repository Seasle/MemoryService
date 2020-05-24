package org.seasle;

import java.util.Date;
import java.util.logging.*;
import java.io.IOException;

public class LoggerProvider {
    private static final LoggerProvider loggerProvider = new LoggerProvider();
    private Logger logger = null;

    public LoggerProvider() {
        try {
            this.logger = Logger.getLogger("logger");

            FileHandler fileHandler = new FileHandler("logs.log", true);
            SimpleFormatter simpleFormatter = new SimpleFormatter() {
                private static final String format = "[%1$tF %1$tT] [%2$s] %3$s%n";

                @Override
                public synchronized String format(LogRecord record) {
                    return String.format(
                        format,
                        new Date(record.getMillis()),
                        record.getLevel().getLocalizedName(),
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
        return loggerProvider.logger;
    }
}