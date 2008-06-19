package org.jvnet.solaris.libzfs.jna;

/**
 * @author Kohsuke Kawaguchi
 */
public class zfs_type_t {
    public static final int FILESYSTEM = 1;
    public static final int SNAPSHOT = 2;
    public static final int VOLUME = 4;
    public static final int POOL = 8;

    public static final int DATASET = FILESYSTEM|VOLUME|SNAPSHOT;
}
