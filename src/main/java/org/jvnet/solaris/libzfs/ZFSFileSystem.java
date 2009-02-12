/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the license at usr/src/OPENSOLARIS.LICENSE
 * or http://www.opensolaris.org/os/licensing.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at usr/src/OPENSOLARIS.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */
package org.jvnet.solaris.libzfs;

import org.jvnet.solaris.libzfs.jna.zfs_handle_t;
import org.jvnet.solaris.libzfs.jna.zfs_prop_t;
import static org.jvnet.solaris.libzfs.jna.libzfs.LIBZFS;
import org.jvnet.solaris.mount.MountFlags;

import java.io.File;

/**
 * ZFS file system.
 *
 * @author Kohsuke Kawaguchi
 */
public final class ZFSFileSystem extends ZFSObject {
    /*package*/ ZFSFileSystem(LibZFS parent, zfs_handle_t handle) {
        super(parent, handle);
    }

    /**
     * Is this dataset mounted.
     *
     * @return is dataset mounted.
     */
    public boolean isMounted() {
        final boolean isMounted = LIBZFS.zfs_is_mounted(handle, null);
        return isMounted;
    }

    /**
     * Gets the mount point of this data set, as indicated by the 'mountpoint' property.
     *
     * @return
     *      null if the mount point is none or legacy, in which case zfs doesn't know
     *      where this is supposed to be mounted.
     */
    public File getMountPoint() {
        String mp = getZfsProperty(zfs_prop_t.ZFS_PROP_MOUNTPOINT);
        if(mp==null || mp.equals("legacy") || mp.equals("none"))
            return null;
        return new File(mp);
    }

    /**
     * Sets the mount point of this data set.
     *
     * <p>
     * The dataset won't be remounted until you manually do so (TODO: verify)
     */
    public void setMountPoint(File loc) {
        setProperty("mountpoint",loc.getAbsolutePath());
    }

    /**
     * Mounts this file system.
     */
    public void mount() {
        mount(0);
    }

    /**
     * Mounts this file system.
     *
     * @param flags
     *      See {@link MountFlags}.
     */
    public void mount(int flags) {
        if (LIBZFS.zfs_mount(handle, null, flags) != 0)
            throw new ZFSException(library,"Failed to mount "+getName());
    }

    /**
     * Unmounts this file system.
     */
    public void unmount() {
        unmount(0);
    }

    /**
     * Unmounts this file system.
     *
     * @param flags
     *      See {@link MountFlags}.
     */
    public void unmount(int flags) {
        if (LIBZFS.zfs_unmount(handle, null, flags) != 0) {
            throw new ZFSException(library,"Failed to unmount "+getName());
        }
    }

    /**
     * Share this dataset.
     */
    public void share() {
        if (LIBZFS.zfs_share(handle) != 0) {
            throw new ZFSException(library);
        }
    }

    /**
     * Unshare this dataset.
     */
    public void unshare() {
        if (LIBZFS.zfs_unshare(handle) != 0) {
            throw new ZFSException(library);
        }
    }
}
