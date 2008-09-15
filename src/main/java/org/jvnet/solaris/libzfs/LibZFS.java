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
import java.io.File;
import org.jvnet.solaris.libzfs.jna.libzfs;
import static org.jvnet.solaris.libzfs.jna.libzfs.LIBZFS;
import org.jvnet.solaris.libzfs.jna.libzfs_handle_t;
import org.jvnet.solaris.libzfs.jna.zfs_handle_t;
import org.jvnet.solaris.libzfs.jna.zfs_type_t;
import org.jvnet.solaris.nvlist.jna.libnvpair;
import org.jvnet.solaris.nvlist.jna.nvlist_t;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.EnumSet;
import java.util.Map;
import java.util.Collections;

/**
 * @author Kohsuke Kawaguchi
 */
public class LibZFS {
    private libzfs_handle_t handle;

    public LibZFS() {
        handle = LIBZFS.libzfs_init();
    }

    /**
     * List up all the root pools and return them.
     *
     * TODO: are they roots that are not pools?
     * @return
     *      can be empty but never null.
     */
    public List<ZFSPool> roots() {
        final List<ZFSPool> r = new ArrayList<ZFSPool>();
        LIBZFS.zfs_iter_root(handle,new libzfs.zfs_iter_f() {
            public int callback(zfs_handle_t handle, Pointer arg) {
                r.add(new ZFSPool(LibZFS.this,handle));
                return 0;
            }
        }, null);
        return r;
    }

    /**
     * Does a zfs dataset of the given name exist?
     */
    public boolean exists(String name) {
        return exists(name,EnumSet.allOf(ZFSType.class));
    }

    /**
     * Does a zfs dataset of the given name and the given type exist?
     */
    public boolean exists(String name, Set<ZFSType> typeMask) {
        int mask = 0;
        for (ZFSType t : typeMask)
            mask |= t.code;
        return LIBZFS.zfs_dataset_exists(handle,name,mask);
    }

    /**
     * Does a zfs dataset of the given name and the given type exist?
     */
    public boolean exists(String name, ZFSType type) {
        return exists(name,EnumSet.of(type));
    }

    public ZFSObject create(String name, ZFSType type) {
        return create(name,type, Collections.<String,String>emptyMap());
    }

    public ZFSObject create(String name, ZFSType type, Map<String,String> props) {
        nvlist_t nvl = nvlist_t.alloc(libnvpair.NV_UNIQUE_NAME);
        for (Map.Entry<String, String> e : props.entrySet()) {
            nvl.put(e.getKey(),e.getValue());
        }

        /* create intermediate directories */
        String[] dirs = name.split(File.separator);
        StringBuffer sb = new StringBuffer(dirs[0]);
        for(int i=1; i < dirs.length; i++) {
          sb.append(File.separator+dirs[i]);
          if ( !exists(sb.toString(), ZFSType.FILESYSTEM)) {
            if(LIBZFS.zfs_create(handle,sb.toString(),type.code,nvl)!=0) {
              throw new ZFSException(this); 
            }
          }
        }

        return open(name);
    }

    /**
     * Opens a ZFS data set of the given name.
     */
    public ZFSObject open(String name) {
        return open(name,zfs_type_t.DATASET);
    }

    public ZFSObject open(String name, int /*zfs_type_t*/ mask ) {
        return new ZFSObject(this,LIBZFS.zfs_open(handle,name, mask));
    }

    /**
     * Returns {@link libzfs_handle_t} that this object wraps.
     * <p>
     * If the caller wants to use methods that don't yet have a high-level binding,
     * the returned {@link libzfs_handle_t} can be used directly in conjunction with
     * {@link libzfs#LIBZFS}.
     */
    public libzfs_handle_t getHandle() {
        return handle;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        dispose();
    }

    /**
     * Eagerly releases the native resource associated with this wrapper,
     * instead of waiting for GC to take care of it.
     */
    public synchronized void dispose() {
        if(handle!=null) {
            LIBZFS.libzfs_fini(handle);
            handle = null;
        }
    }
}
