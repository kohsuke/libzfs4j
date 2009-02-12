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
import org.jvnet.solaris.libzfs.jna.zfs_prop_t;
import org.jvnet.solaris.libzfs.jna.zfs_type_t;
import org.jvnet.solaris.nvlist.jna.nvlist_t;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Represents ZFS snapshot, file system, volume, or pool.
 * 
 * @author Kohsuke Kawaguchi
 */
public abstract class ZFSObject implements Comparable<ZFSObject>, ZFSContainer {

    /*package*/ final LibZFS library;
    /*package*/ zfs_handle_t handle;

    ZFSObject(final LibZFS library, final zfs_handle_t handle) {
        this.library = library;
        if (handle == null) {
            throw new ZFSException(library);
        }
        this.handle = handle;
    }

    /**
     * Instantiate the right subtype.
     */
    /*package*/ static ZFSObject create(LibZFS parent, zfs_handle_t handle) {
        switch (ZFSType.fromCode(LIBZFS.zfs_get_type(handle))) {
        case FILESYSTEM:    return new ZFSFileSystem(parent,handle);
        case POOL:          return new ZFSPool(parent,handle);
        case SNAPSHOT:      return new ZFSSnapshot(parent,handle);
        case VOLUME:        return new ZFSVolume(parent,handle);
        default:            throw new AssertionError();
        }
    }

    public LibZFS getLibrary() {
        return library;
    }

    public int compareTo(ZFSObject that) {
        long a = Long.parseLong(this.getZfsProperty(zfs_prop_t.ZFS_PROP_CREATETXG));
        long b = Long.parseLong(that.getZfsProperty(zfs_prop_t.ZFS_PROP_CREATETXG));

        if (a > b) {
            return 1;
        }
        if (a < b) {
            return -1;
        }
        return 0;
    }

    public List<ZFSObject> children() {
        return children(ZFSObject.class);
    }

    public <T extends ZFSObject> List<T> children(Class<T> type) {
        return children(type, new ArrayList<T>(), false);
    }

    public List<ZFSObject> descendants() {
        return children(ZFSObject.class);
    }

    public <T extends ZFSObject> List<T> descendants(Class<T> type) {
        return children(type, new ArrayList<T>(), true);
    }

    private <T extends ZFSObject> List<T> children(Class<T> type, List<T> list, boolean recursive) {
        for (ZFSObject snap : snapshots()) {
            if(type.isInstance(snap))
                list.add(type.cast(snap));
        }
        for (ZFSObject child : getChildren()) {
            if (!child.getName().contains("@")) {
                if(type.isInstance(child))
                    list.add(type.cast(child));
                if(recursive)
                    child.children(type,list,recursive);
            }
        }
        return list;
    }

    /**
     * Creates a clone from this snapshot.
     * 
     * This method fails if this {@link ZFSObject} is not a snapshot.
     */
    public ZFSFileSystem clone(String fullDestinationName) {
        if (LIBZFS.zfs_clone(handle, fullDestinationName, null) != 0)
            throw new ZFSException(library);
        ZFSFileSystem target = (ZFSFileSystem) library.open(fullDestinationName);
        // this behavior mimics "zfs clone"
        target.mount();
        target.share();
        return target;
    }

    /**
     * Creates a nested file system.
     *
     * @param props
     *      ZFS properties to be attached to the new file system. Can be null.
     */
    public ZFSFileSystem createFileSystem(final String name, final Map<String, String> props) {
        return (ZFSFileSystem) library.create(getName()+'/'+name, ZFSType.FILESYSTEM,props);
    }

    /**
     * Opens a nested file system.
     */
    public ZFSFileSystem openFileSystem(String name) {
        return library.open(getName()+'/'+name,ZFSFileSystem.class);
    }

    /**
     * Take a snapshot of this ZFS dataset.
     * 
     * @param snapshotName
     *            the name of the Snapshot to create, i.e. 'monday',
     *            'before-test'.
     * @return the created snapshot.
     */
    public ZFSSnapshot createSnapshot(final String snapshotName) {
        final ZFSSnapshot dataSet = createSnapshot(snapshotName, false);
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
    public ZFSSnapshot createSnapshot(final String snapshotName,
            final boolean recursive) {
        String fullName = getName() + '@' + snapshotName;
        /*
         * nv96 prototype: if(LIBZFS.zfs_snapshot(library.getHandle(),
         * fullName,recursive, null)!=0) pre-nv96 prototype:
         * if(LIBZFS.zfs_snapshot(library.getHandle(), fullName,recursive)!=0)
         */
        if (LIBZFS.zfs_snapshot(library.getHandle(), fullName, recursive, null) != 0) {
            throw new ZFSException(library);
        }

        final ZFSSnapshot dataSet = (ZFSSnapshot) library.open(fullName, zfs_type_t.SNAPSHOT);
        return dataSet;
    }

    /**
     * Wipes out the dataset and all its data. Very dangerous.
     */
    public void destory() {
        if (LIBZFS.zfs_destroy(handle) != 0)
            throw new ZFSException(library);
    }

    public synchronized void dispose() {
        if (handle != null)
            LIBZFS.zfs_close(handle);
        handle = null;
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
                r.add(ZFSObject.create(library, handle));
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
                list.add(ZFSObject.create(library, handle));
                return 0;
            }
        }, null);
        return list;
    }

    /**
     * Gets the name of the dataset like "rpool/foo/bar".
     * 
     * @return the name of the dataset.
     */
    public String getName() {
        final String zfsName = LIBZFS.zfs_get_name(handle);
        return zfsName;
    }

    /**
     * Gets the type of this {@link ZFSObject}.
     */
    public ZFSType getType() {
        return ZFSType.fromCode(LIBZFS.zfs_get_type(handle));
    }

    public Map<zfs_prop_t,String> getZfsProperty(List<zfs_prop_t> props) {
        Memory propbuf = new Memory(libzfs.ZFS_MAXPROPLEN);
        char[] buf = null;
        IntByReference ibr = null;

        Hashtable<zfs_prop_t, String> map = new Hashtable<zfs_prop_t, String>();
        for (zfs_prop_t prop : props) {
            int ret = LIBZFS.zfs_prop_get(handle, new NativeLong(prop.ordinal()),
                    propbuf, libzfs.ZFS_MAXPROPLEN, ibr, buf,
                    new NativeLong(0), true);
            if(ret==0)
                map.put(prop, propbuf.getString(0));
        }
        return map;
    }

    public String getZfsProperty(zfs_prop_t prop) {
        Memory propbuf = new Memory(libzfs.ZFS_MAXPROPLEN);
        char[] buf = null;
        IntByReference ibr = null;

        int ret = LIBZFS.zfs_prop_get(handle, new NativeLong(prop.ordinal()),
                propbuf, libzfs.ZFS_MAXPROPLEN, ibr, buf,
                new NativeLong(0), true);

        return ((ret != 0) ? null : propbuf.getString(0));
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
            throw new ZFSException(library);
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
     * Renames this data set to another name.
     *
     * @return
     *      {@link ZFSObject} representing the new renamed dataset.
     */
    public ZFSObject rename(String fullName, boolean recursive) {
        if (LIBZFS.zfs_rename(handle, fullName, recursive) != 0)
            throw new ZFSException(library);

        return library.open(fullName);
    }

    public ZFSObject rollback(boolean recursive) {
        String filesystem = getName().substring(0, getName().indexOf("@"));
        ZFSObject fs = library.open(filesystem);
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
            throw new ZFSException(library);

        return library.open(filesystem);
    }

    /**
     * Sets a user-defined property.
     */
    public void setProperty(String key, String value) {
        if (LIBZFS.zfs_prop_set(handle, key, value) != 0)
            throw new ZFSException(library,"Failed to set property "+key+" on "+getName());
    }

    /**
     * Obtain all snapshots for this dataset.
     * 
     * @return all snapshot datasets.
     */
    public Set<ZFSSnapshot> snapshots() {
        final Set<ZFSSnapshot> set = new TreeSet<ZFSSnapshot>();
        LIBZFS.zfs_iter_snapshots(handle, new libzfs.zfs_iter_f() {
            public int callback(zfs_handle_t handle, Pointer arg) {
                set.add((ZFSSnapshot)ZFSObject.create(library, handle));
                return 0;
            }
        }, null);
        return set;
    }

    /**
     * Grants the specified set of permissions to this dataset.
     */
    public void allow(ACLBuilder acl) {
        if(LIBZFS.zfs_perm_set(handle,acl.toNativeFormat())!=0)
            throw new ZFSException(library);
    }

    /**
     * Revokes the specified set of permissions to this dataset.
     */
    public void unallow(ACLBuilder acl) {
        if(LIBZFS.zfs_perm_set(handle,acl.toNativeFormat())!=0)
            throw new ZFSException(library);
    }


    /**
     * Returns {@link #getName() the name}.
     */
    @Override
    public String toString() {
        return getName();
    }
}
