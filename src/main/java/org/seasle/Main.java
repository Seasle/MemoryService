package org.seasle;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Main {
    private static final Logger logger = LoggerProvider.getInstance();
    private static final Logger report = ReportProvider.getInstance();
    private static Database database = null;
    private static GUI gui = null;
    private static HashMap<String, Object> options = null;

    public static void main(String[] args) {
        if (lockInstance("MemoryService")) {
            database = new Database("database");
            options = database.getOptions();

            if (options != null) {
                gui = new GUI(database, options);

                Thread thread = new Thread(Main::collectStatistics);
                thread.start();

                Runtime.getRuntime().addShutdownHook(new Thread(Main::exit));
            } else {
                logger.log(Level.SEVERE, "Options not found.");

                System.exit(1);
            }
        } else {
            System.exit(0);
        }
    }

    private static void collectStatistics() {
        while (true) {
            Statistics statistics = new Statistics();

            Set<String> disks = statistics.getDisks();
            double threshold = (double) options.get("threshold");

            for (String disk : disks) {
                DiskInfo diskInfo = statistics.getDiskInfo(disk);

                database.putData(statistics.getDiskInfo(disk));

                double percent = (double) diskInfo.used / (double) diskInfo.total;
                if (percent > threshold) {
                    report.info(String.format("На диске %s использовано %d процент (-ов)", disk, (int) (percent * 100)));
                }
            }

            if (gui.formManager.isVisible()) {
                gui.formManager.updateInterface();
            }

            try {
                Thread.sleep(Long.valueOf((int) options.get("interval")));
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