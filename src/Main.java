import java.util.Set;

public class Main {
    public static Database database = new Database("database");

    public static void main(String[] args) {
        Statistics statistics = new Statistics();

        Set<String> disks = statistics.getDisks();
        for (String disk : disks) {
            database.put(statistics.getDiskInfo(disk));
        }
    }

    public static void exit(int status) {
        database.close();
    }
}
