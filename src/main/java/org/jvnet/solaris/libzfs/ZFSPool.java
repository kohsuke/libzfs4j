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
 * ZFS pool, which is a top-level object of the ZFS data sets.
 *
 * @author Kohsuke Kawaguchi
 */
public final class ZFSPool extends ZFSObject {
    ZFSPool(final LibZFS parent, final zfs_handle_t handle) {
        super(parent, handle);
    }

    public String getZpoolProperty(zpool_prop_t prop) {
        Memory propbuf = new Memory(libzfs.ZPOOL_MAXPROPLEN);
        zpool_handle_t zpool_handle = LIBZFS.zpool_open(library.getHandle(),
                this.getName());
        int ret = LIBZFS.zpool_get_prop(zpool_handle, new NativeLong(prop
                .ordinal()), propbuf, new NativeLong(
                libzfs.ZPOOL_MAXPROPLEN), null);
        return ((ret != 0) ? null : propbuf.getString(0));
    }
}
