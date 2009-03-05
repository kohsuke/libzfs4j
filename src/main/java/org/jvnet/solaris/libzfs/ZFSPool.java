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
import org.jvnet.solaris.libzfs.jna.zpool_prop_t;
import org.jvnet.solaris.libzfs.jna.libzfs;
import org.jvnet.solaris.libzfs.jna.zpool_handle_t;
import static org.jvnet.solaris.libzfs.jna.libzfs.LIBZFS;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;

/**
 * zpool, which is a storage abstraction.
 *
 * @author Kohsuke Kawaguchi
 */
public final class ZFSPool {
    /*package*/ final LibZFS library;
    /*package*/ zpool_handle_t handle;
    private final String name;

    ZFSPool(final LibZFS parent, final zpool_handle_t handle) {
        this.library = parent;
        this.handle = handle;
        this.name = LIBZFS.zpool_get_name(handle);
    }

    public String getName() {
        return name;
    }

    public String getProperty(zpool_prop_t prop) {
        Memory propbuf = new Memory(libzfs.ZPOOL_MAXPROPLEN);
        int ret = LIBZFS.zpool_get_prop(handle, new NativeLong(prop
                .ordinal()), propbuf, new NativeLong(
                libzfs.ZPOOL_MAXPROPLEN), null);
        return ((ret != 0) ? null : propbuf.getString(0));
    }

    /**
     * Disables datasets within a pool by unmounting/unsharing them all.
     *
     * @param force
     *      Not exactly sure what this does.
     */
    public void disableDatasets(boolean force) {
        check(LIBZFS.zpool_disable_datasets(handle,force));
    }

    private void check(int r) {
        if(r!=0)
            throw new ZFSException(library);
    }

    /**
     * Does "zpool export".
     */
    public void export(boolean force, boolean hardForce) {
        disableDatasets(force);
        if(hardForce)
            check(LIBZFS.zpool_export_force(handle));
        else
            check(LIBZFS.zpool_export(handle,force));
    }

    public synchronized void dispose() {
        if (handle != null)
            LIBZFS.zpool_close(handle);
        handle = null;
    }

    @Override
    protected void finalize() throws Throwable {
        dispose();
        super.finalize();
    }
}
