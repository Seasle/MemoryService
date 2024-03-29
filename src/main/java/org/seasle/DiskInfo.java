package org.seasle;

import java.io.File;

public class DiskInfo {
    public String name;
    public Long total;
    public Long free;
    public Long usable;
    public Long used;

    public DiskInfo(String name, String path) {
        File disk = new File(path);

        this.name = name;
        this.total = disk.getTotalSpace();
        this.free = disk.getFreeSpace();
        this.usable = disk.getUsableSpace();
        this.used = this.total - this.usable;
    }
}
