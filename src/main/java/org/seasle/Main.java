package org.seasle;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.channels.FileLock;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Main {
    private static final Logger logger = LoggerProvider.getInstance();
    private static Database database = null;
    private static FormManager formManager = null;

    public static void main(String[] args) {
        if (lockInstance("MemoryService")) {
            database = new Database("database");
            formManager = FormManager.getInstance();

            formManager.setDatabase(database);

            Thread thread = new Thread(Main::collectStatistics);
            thread.start();

            Runtime.getRuntime().addShutdownHook(new Thread(Main::exit));
        } else {
            System.exit(0);
        }
    }

    private static void collectStatistics() {
        while (true) {
            Statistics statistics = new Statistics();

            Set<String> disks = statistics.getDisks();
            for (String disk : disks) {
                database.putData(statistics.getDiskInfo(disk));
            }

            if (formManager.isVisible()) {
                formManager.updateInterface();
            }

            try {
                Thread.sleep(60 * 1000);
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }
    }

    private static boolean lockInstance(final String lockFile) {
        try {
            final File file = new File(lockFile);
            final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            final FileLock fileLock = randomAccessFile.getChannel().tryLock();

            if (fileLock != null) {
                Files.setAttribute(file.toPath(), "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        fileLock.release();
                        randomAccessFile.close();
                        file.delete();
                    } catch (Exception exception) {
                        logger.log(Level.SEVERE, String.format("Unable to remove lock file: %s", lockFile));
                    }
                }));

                return true;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, String.format("Unable to create and/or lock file: %s", lockFile));
        }

        return false;
    }

    private static void exit() {
        database.close();
    }
}