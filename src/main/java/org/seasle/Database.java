package org.seasle;

import java.sql.*;
import java.net.URL;
import java.time.Instant;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Database {
    private final Logger logger = LoggerProvider.getInstance();
    private Connection connection = null;

    public Database(String name) {
        try {
            URL path = getClass().getResource(String.format("/%s.db", name));
            this.connection = DriverManager.getConnection(String.format("jdbc:sqlite::resource:%s", path));

            logger.log(Level.INFO, "Connection has been successfully opened.");
        } catch (SQLException exception) {
            logger.log(Level.SEVERE, exception.getMessage());
        }
    }

    public ResultSet getAllData() {
        if (this.connection != null) {
            String query = "SELECT * FROM statistics";

            try {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                statement.closeOnCompletion();

                logger.log(Level.INFO, String.format("Data has been extracted successfully. Query: `%s`", query));

                return resultSet;
            } catch (SQLException exception) {
                logger.log(Level.SEVERE, exception.getMessage());

                return null;
            }
        } else {
            logger.log(Level.WARNING, "Data cannot be get, because connection hasn't been opened.");

            return null;
        }
    }

    public void put(Info info) {
        if (this.connection != null) {
            Instant timestamp = Instant.now();
            String query = String.format(
                "INSERT INTO statistics (name, total, free, usable, timestamp) VALUES ('%s', %d, %d, %d, %d)",
                info.name,
                info.total,
                info.free,
                info.usable,
                timestamp.getEpochSecond()
            );

            try {
                Statement statement = this.connection.createStatement();
                statement.executeUpdate(query);
                statement.closeOnCompletion();

                logger.log(Level.INFO, String.format("Data has been added successfully. Query: `%s`", query));
            } catch (SQLException exception) {
                logger.log(Level.SEVERE, exception.getMessage());
            }
        } else {
            logger.log(Level.WARNING, "Data cannot be put, because connection hasn't been opened.");
        }
    }

    public void close() {
        try {
            this.connection.close();
        } catch (SQLException exception) {
            logger.log(Level.SEVERE, exception.getMessage());
        }
    }
}
