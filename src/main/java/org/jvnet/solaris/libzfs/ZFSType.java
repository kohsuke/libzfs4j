package org.jvnet.solaris.libzfs;

import org.jvnet.solaris.libzfs.jna.zfs_type_t;

/**
 * @author Kohsuke Kawaguchi
 * @see zfs_type_t
 */
public enum ZFSType {
    FILESYSTEM(1),
    SNAPSHOT(2),
    VOLUME(4),
    POOL(8);

    ZFSType(int code) {
        this.code = code;
    }

    public final int code;
}
