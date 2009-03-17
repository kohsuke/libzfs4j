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

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.PointerByReference;
import org.jvnet.solaris.libzfs.jna.libzfs;
import static org.jvnet.solaris.libzfs.jna.libzfs.LIBZFS;
import org.jvnet.solaris.libzfs.jna.zpool_handle_t;
import org.jvnet.solaris.libzfs.jna.zpool_prop_t;

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

    public ZPoolStatus getStatus() {
        return ZPoolStatus.values()[LIBZFS.zpool_get_status(handle,new PointerByReference())];
    }

    /**
     * Gets the total size of this pool in bytes.
     *
     * <p>
     * Because of the way libzfs report the size information
     * (as strings like 1.2G), the precision of this information is low.
     */
    public long getSize() {
        return toSize(getProperty(zpool_prop_t.ZPOOL_PROP_SIZE));
    }

    /**
     * Gets the remaining free space size of this pool in bytes.
     *
     * <p>
     * Because of the way libzfs report the size information
     * (as strings like 1.2G), the precision of this information is low.
     */
    public long getAvailableSize() {
        return toSize(getProperty(zpool_prop_t.ZPOOL_PROP_AVAILABLE));
    }

    /**
     * Gets the size of this pool that's already used in bytes.
     *
     * <p>
     * Because of the way libzfs report the size information
     * (as strings like 1.2G), the precision of this information is low.
     */
    public long getUsedSize() {
        return toSize(getProperty(zpool_prop_t.ZPOOL_PROP_USED));
    }

    /**
     * @param value
     *      String that represents a size
     */
    private long toSize(String value) {
        value = value.toUpperCase();
        double d = Double.parseDouble(value.substring(0,value.length()-1));
        long multiplier = 1;
        switch(value.charAt(value.length()-1)) {
        case 'P':   multiplier *= 1024; // fall through
        case 'T':   multiplier *= 1024; // fall through
        case 'G':   multiplier *= 1024; // fall through
        case 'M':   multiplier *= 1024; // fall through
        case 'K':   multiplier *= 1024; // fall through
        }

        return (long)(d*multiplier);
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
