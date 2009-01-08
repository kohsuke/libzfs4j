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
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import org.jvnet.solaris.libzfs.jna.libzfs;
import static org.jvnet.solaris.libzfs.jna.libzfs.LIBZFS;
import org.jvnet.solaris.libzfs.jna.zfs_handle_t;
import org.jvnet.solaris.libzfs.jna.zfs_type_t;
import org.jvnet.solaris.nvlist.jna.nvlist_t;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.jvnet.solaris.jna.EnumByReference;
import org.jvnet.solaris.libzfs.jna.zfs_prop_t;
import org.jvnet.solaris.libzfs.jna.zpool_handle_t;
import org.jvnet.solaris.libzfs.jna.zpool_prop_t;

/**
 * Represents ZFS snapshot, file system, volume, or pool.
 * 
 * @author Kohsuke Kawaguchi
 */
public class ZFSObject implements Comparator<ZFSObject> {

    private final LibZFS parent;
    private zfs_handle_t handle;

    ZFSObject(final LibZFS parent, final zfs_handle_t handle) {
        this.parent = parent;
        if (handle == null) {
            throw new ZFSException(parent);
        }
        this.handle = handle;
    }

    public int compare(ZFSObject o1, ZFSObject o2) {
        long a = Long.parseLong(o1
                .getZfsProperty(zfs_prop_t.ZFS_PROP_CREATETXG));
        long b = Long.parseLong(o2
                .getZfsProperty(zfs_prop_t.ZFS_PROP_CREATETXG));

        if (a > b) {
            return 1;
        }
        if (a < b) {
            return -1;
        }
        return 0;
    }

    public List<ZFSObject> children() {
        final List<ZFSObject> list = new ArrayList<ZFSObject>();
        final List<ZFSObject> children = children(list, this);
        return children;
    }

    // todo should this be static/private as only uses method parameters
    public List<ZFSObject> children(List<ZFSObject> list, ZFSObject zfs) {
        for (ZFSObject snap : zfs.snapshots()) {
            list.add(snap);
        }
        for (ZFSObject child : zfs.getChildren()) {
            if (!child.getName().contains("@")) {
                list.add(child);
                children(list, child);
            }
        }
        return list;
    }

    /**
     * Creates a clone from this snapshot.
     * 
     * This method fails if this {@link ZFSObject} is not a snapshot.
     */
    public ZFSObject clone(String fullDestinationName) {
        if (LIBZFS.zfs_clone(handle, fullDestinationName, null) != 0)
            throw new ZFSException(parent);
        ZFSObject target = parent.open(fullDestinationName);
        // this behavior mimics "zfs clone"
        target.mount();
        target.share();
        return target;
    }

    /**
     * Take a snapshot of this ZFS dataset.
     * 
     * @param snapshotName
     *            the name of the Snapshot to create, i.e. 'monday',
     *            'before-test'.
     * @return the created snapshot.
     */
    public ZFSObject createSnapshot(final String snapshotName) {
        final ZFSObject dataSet = createSnapshot(snapshotName, false);
        return dataSet;
    }

    /**
     * Take a snapshot of this ZFS dataset and recursively for all child
     * datasets.
     * 
     * @param snapshotName
     *            the name of the Snapshot to create, i.e. 'monday',
     *            'before-test'.
     * @param recursive
     *            should snapshot recursively create snapshot for all descendant
     *            datasets. Snapshots are taken atomically, so that all
     *            recursive snapshots correspond to the same moment in time.
     * @return the created snapshot of this dataset.
     */
    public ZFSObject createSnapshot(final String snapshotName,
            final boolean recursive) {
        String fullName = getName() + '@' + snapshotName;
        /*
         * nv96 prototype: if(LIBZFS.zfs_snapshot(parent.getHandle(),
         * fullName,recursive, null)!=0) pre-nv96 prototype:
         * if(LIBZFS.zfs_snapshot(parent.getHandle(), fullName,recursive)!=0)
         */
        if (LIBZFS.zfs_snapshot(parent.getHandle(), fullName, recursive, null) != 0) {
            throw new ZFSException(parent);
        }

        final ZFSObject dataSet = parent.open(fullName, zfs_type_t.SNAPSHOT);
        return dataSet;
    }

    /**
     * Wipes out the dataset and all its data. Very dangerous.
     */
    public void destory() {
        if (LIBZFS.zfs_destroy(handle) != 0)
            throw new ZFSException(parent);
    }

    public synchronized void dispose() {
        if (handle != null)
            LIBZFS.zfs_close(handle);
        handle = null;
    }

    public boolean equals(Object a, Object b) {
        // todo would using zfs_prop_t.ZFS_PROP_CREATETXG be more accurate?
        if (a == b) {
            return true;
        }
        return false;
    }

    @Override
    public final boolean equals(Object o) {
        // todo would using zfs_prop_t.ZFS_PROP_CREATETXG be more accurate?
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ZFSObject zfsObject = (ZFSObject) o;
        final boolean equals = handle.equals(zfsObject.handle);
        return equals;
    }

    public List<ZFSObject> filesystems() {
        final List<ZFSObject> r = new ArrayList<ZFSObject>();
        LIBZFS.zfs_iter_filesystems(handle, new libzfs.zfs_iter_f() {
            public int callback(zfs_handle_t handle, Pointer arg) {
                r.add(new ZFSObject(parent, handle));
                return 0;
            }
        }, null);
        return r;
    }

    @Override
    protected void finalize() throws Throwable {
        dispose();
        super.finalize();
    }

    public List<ZFSObject> getChildren() {
        final List<ZFSObject> list = new ArrayList<ZFSObject>();
        LIBZFS.zfs_iter_children(handle, new libzfs.zfs_iter_f() {
            public int callback(zfs_handle_t handle, Pointer arg) {
                list.add(new ZFSObject(parent, handle));
                return 0;
            }
        }, null);
        return list;
    }

    /**
     * Gets the name of the dataset.
     * 
     * @return the name of the dataset.
     */
    public String getName() {
        final String zfsName = LIBZFS.zfs_get_name(handle);
        return zfsName;
    }

    public Hashtable<zfs_prop_t, String> getZfsProperty(List<zfs_prop_t> props) {
        Memory propbuf = new Memory(libzfs.ZFS_MAXPROPLEN);
        char[] buf = null;
        IntByReference ibr = null;
        int ret = 0;

        Hashtable<zfs_prop_t, String> map = new Hashtable<zfs_prop_t, String>();
        for (zfs_prop_t prop : props) {
            ret = LIBZFS.zfs_prop_get(handle, new NativeLong(prop.ordinal()),
                    (Pointer) propbuf, libzfs.ZFS_MAXPROPLEN, ibr, buf,
                    new NativeLong(0), true);
            map.put(prop, (ret != 0) ? "-" : propbuf.getString(0));
        }
        return map;
    }

    public String getZfsProperty(zfs_prop_t prop) {
        Memory propbuf = new Memory(libzfs.ZFS_MAXPROPLEN);
        char[] buf = null;
        IntByReference ibr = null;

        int ret = LIBZFS.zfs_prop_get(handle, new NativeLong(prop.ordinal()),
                (Pointer) propbuf, libzfs.ZFS_MAXPROPLEN, ibr, buf,
                new NativeLong(0), true);

        return ((ret != 0) ? "-" : propbuf.getString(0));
    }

    public String getZpoolProperty(zpool_prop_t prop) {
        Memory propbuf = new Memory(libzfs.ZPOOL_MAXPROPLEN);
        char[] buf = null;
        EnumByReference ebr = null;

        zpool_handle_t zpool_handle = LIBZFS.zpool_open(parent.getHandle(),
                this.getName());
        int ret = LIBZFS.zpool_get_prop(zpool_handle, new NativeLong(prop
                .ordinal()), (Pointer) propbuf, new NativeLong(
                libzfs.ZPOOL_MAXPROPLEN), ebr);
        return ((ret != 0) ? "-" : propbuf.getString(0));
    }

    public Hashtable<String, String> getUserProperty(List<String> keys) {
        // don't we need to release userProps later?
        Hashtable<String, String> map = new Hashtable<String, String>();

        nvlist_t userProps = LIBZFS.zfs_get_user_props(handle);
        for (String key : keys) {
            nvlist_t v = userProps.getNVList(key);
            if (v == null)
                return null;
            map.put(key, v.getString("value"));
        }
        return map;
    }

    public String getUserProperty(String key) {
        // don't we need to release userProps later?
        nvlist_t userProps = LIBZFS.zfs_get_user_props(handle);
        nvlist_t v = userProps.getNVList(key);
        if (v == null)
            return null;

        return v.getString("value");
    }

    @Override
    public final int hashCode() {
        return handle.hashCode();
    }

    public void inheritProperty(String key) {
        // Note: create new object after calling this method to reflect
        // inherited property.
        // System.out.println("key "+key+" = "+getUserProperty(key));
        if (LIBZFS.zfs_prop_inherit(handle, key) != 0)
            throw new ZFSException(parent);
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
     * Is this dataset shared.
     * 
     * @return is dataset shared.
     */
    public boolean isShared() {
        throw new UnsupportedOperationException("Not supported yet.");
        // final boolean isShared = LIBZFS.zfs_is_shared(handle);
        // return isShared;
    }

    /**
     * Mounts this dataset.
     */
    public void mount() {
        if (LIBZFS.zfs_mount(handle, null, 0) != 0) {
            throw new ZFSException(parent);
        }
    }

    public ZFSObject rename(String fullName, int /* zfs_type_t */type,
            boolean recursive) {
        if (LIBZFS.zfs_rename(handle, fullName, recursive) != 0)
            throw new ZFSException(parent);

        return parent.open(fullName, type);
    }

    public ZFSObject rollback(boolean recursive) {
        String filesystem = getName().substring(0, getName().indexOf("@"));
        ZFSObject fs = parent.open(filesystem);
        if (recursive) {
            /* first pass - check for clones */
            List<ZFSObject> list = fs.getChildren();
            for (ZFSObject child : list) {
                if (!child.getName().startsWith(filesystem + "@")) {
                    return child;
                }
            }
            /* second pass - find snapshot index, destroy later snapshots */
            boolean found = false;
            for (ZFSObject snap : fs.snapshots()) {
                String name = snap.getName();
                if (name.equals(getName())) {
                    found = true;
                    continue;
                }
                if (found) {
                    snap.destory();
                }
            }
        }
        if (LIBZFS.zfs_rollback(fs.handle, handle, recursive) != 0)
            throw new ZFSException(parent);

        return parent.open(filesystem);
    }

    /**
     * Sets a user-defined property.
     */
    public void setProperty(String key, String value) {
        if (LIBZFS.zfs_prop_set(handle, key, value) != 0)
            throw new ZFSException(parent);
    }

    /**
     * Share this dataset.
     */
    public void share() {
        if (LIBZFS.zfs_share(handle) != 0) {
            throw new ZFSException(parent);
        }
    }

    /**
     * Obtain all snapshots for this dataset.
     * 
     * @return all snapshot datasets.
     */
    public Set<ZFSObject> snapshots() {
        final Set<ZFSObject> set = new TreeSet<ZFSObject>(this);
        LIBZFS.zfs_iter_snapshots(handle, new libzfs.zfs_iter_f() {
            public int callback(zfs_handle_t handle, Pointer arg) {
                set.add(new ZFSObject(parent, handle));
                return 0;
            }
        }, null);
        return set;
    }

    /**
     * Unmounts this dataset.
     */
    public void unmount() {
        if (LIBZFS.zfs_unmount(handle, null, 0) != 0) {
            throw new ZFSException(parent);
        }
    }

    /**
     * Unshare this dataset.
     */
    public void unshare() {
        if (LIBZFS.zfs_unshare(handle) != 0) {
            throw new ZFSException(parent);
        }
    }

}
