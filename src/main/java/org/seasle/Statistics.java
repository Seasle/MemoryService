package org.seasle;

import java.io.File;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Statistics {
    private final HashMap<String, Info> disks = new HashMap<String, Info>();

    public Statistics() {
        Pattern pattern = Pattern.compile("(\\w)*+");

        File[] roots = File.listRoots();
        for (File root : roots) {
            String path = root.getAbsolutePath();
            Matcher matcher = pattern.matcher(path);

            if (matcher.find()) {
                String key = matcher.group(0);

                this.disks.put(key, new Info(key, path));
            }
        }
    }

    public Set<String> getDisks() {
        return this.disks.keySet();
    }

    public Info getDiskInfo(String name) {
        return this.disks.get(name);
    }
}
