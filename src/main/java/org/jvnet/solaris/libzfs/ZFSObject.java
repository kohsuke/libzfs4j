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
