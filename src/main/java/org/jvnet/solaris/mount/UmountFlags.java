package org.jvnet.solaris.mount;

/**
 * Flag bits passed to mount(2).
 */
public class UmountFlags {
    /**
     * Read-only
     */
    public static final int MS_RDONLY = 0x0001;
    /**
     * Old (4-argument) mount (compatibility)
     */
    public static final int MS_FSS = 0x0002;
    /**
     * 6-argument mount
     */
    public static final int MS_DATA = 0x0004;
    /**
     * Setuid programs disallowed
     */
    public static final int MS_NOSUID = 0x0010;
    /**
     * Remount
     */
    public static final int MS_REMOUNT = 0x0020;
    /**
     * Return ENAMETOOLONG for long filenames
     */
    public static final int MS_NOTRUNC = 0x0040;
    /**
     * Allow overlay mounts
     */
    public static final int MS_OVERLAY = 0x0080;
    /**
     * Data is a an in/out option string
     */
    public static final int MS_OPTIONSTR = 0x0100;
    /**
     * Clustering: Mount into global name space
     */
    public static final int MS_GLOBAL = 0x0200;
    /**
     * Forced unmount
     */
    public static final int MS_FORCE = 0x0400;
    /**
     * Don't show mount in mnttab
     */
    public static final int MS_NOMNTTAB = 0x0800;
}
