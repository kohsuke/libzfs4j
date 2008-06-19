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

import com.sun.jna.Pointer;
import org.jvnet.solaris.libzfs.jna.libzfs;
import static org.jvnet.solaris.libzfs.jna.libzfs.LIBZFS;
import org.jvnet.solaris.libzfs.jna.zfs_handle_t;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
public class ZFSObject {
    private zfs_handle_t handle;

    ZFSObject(zfs_handle_t handle) {
        if(handle==null)
            throw new ZFSException();
        this.handle = handle;
    }

    public String getName() {
        return LIBZFS.zfs_get_name(handle);
    }

    public List<ZFSObject> children() {
        final List<ZFSObject> r = new ArrayList<ZFSObject>();
        LIBZFS.zfs_iter_children(handle, new libzfs.zfs_iter_f() {
            public int callback(zfs_handle_t handle, Pointer arg) {
                r.add(new ZFSObject(handle));
                return 0;
            }
        }, null );
        return r;
    }

    public void mount() {
        if(LIBZFS.zfs_mount(handle, null, 0)!=0)
            throw new ZFSException();
    }

    public void share() {
        if(LIBZFS.zfs_share(handle)!=0)
            throw new ZFSException();
    }

    /**
     * Unmounts this dataset.
     */
    public void unmount() {
        if(LIBZFS.zfs_unmount(handle, null, 0)!=0)
            throw new ZFSException();
    }

    /**
     * Wipes out the dataset and all its data. Very dangerous.
     */
    public void destory() {
        if(LIBZFS.zfs_destroy(handle)!=0)
            throw new ZFSException();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ZFSObject zfsObject = (ZFSObject) o;
        return handle.equals(zfsObject.handle);

    }

    @Override
    public final int hashCode() {
        return handle.hashCode();
    }

    public synchronized void dispose() {
        if(handle!=null)
            LIBZFS.zfs_close(handle);
        handle = null;
    }

    @Override
    protected void finalize() throws Throwable {
        dispose();
        super.finalize();
    }
}
