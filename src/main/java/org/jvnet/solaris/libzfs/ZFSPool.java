package org.jvnet.solaris.libzfs;

import org.jvnet.solaris.libzfs.jna.zfs_handle_t;

/**
 * @author Kohsuke Kawaguchi
 */
public final class ZFSPool extends ZFSObject {
    ZFSPool(zfs_handle_t handle) {
        super(handle);
    }
}
