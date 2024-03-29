package org.seasle;

import java.sql.*;
import java.net.URL;
import java.time.Instant;
import java.util.HashMap;
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

    public HashMap<String, Object> getOptions() {
        if (this.connection != null) {
            String query = "SELECT key, value FROM options";

            try {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                statement.closeOnCompletion();

                logger.log(Level.INFO, String.format("Data has been extracted successfully. Query: `%s`", query));

                HashMap<String, Object> options = new HashMap<>();

                while (resultSet.next()) {
                    options.put(
                        resultSet.getString("key"),
                        resultSet.getObject("value")
                    );
                }

                return options;
            } catch (SQLException exception) {
                logger.log(Level.SEVERE, exception.getMessage());

                return null;
            }
        } else {
            logger.log(Level.WARNING, "Data cannot be get, because connection hasn't been opened.");

            return null;
        }
    }

    public void saveOption(String key, Object value) {
        if (this.connection != null) {
            String query = String.format("UPDATE options SET value = %s WHERE key = '%s'", value, key);

            try {
                Statement statement = connection.createStatement();
                statement.executeUpdate(query);
                statement.closeOnCompletion();

                logger.log(Level.INFO, String.format("Data has been extracted successfully. Query: `%s`", query));
            } catch (SQLException exception) {
                logger.log(Level.SEVERE, exception.getMessage());
            }
        } else {
            logger.log(Level.WARNING, "Data cannot be get, because connection hasn't been opened.");
        }
    }

    public ResultSet getData(String diskName, Long from, Long to) {
        if (this.connection != null) {
            String query = String.format("SELECT total, free, usable, timestamp FROM statistics WHERE name = '%s' AND (timestamp BETWEEN %d AND %d) ORDER BY timestamp", diskName, from, to);

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

    public ResultSet getDisks() {
        if (this.connection != null) {
            String query = "SELECT DISTINCT name FROM statistics";

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

    public void putData(DiskInfo info) {
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