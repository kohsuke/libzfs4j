package org.jvnet.solaris.libzfs.jna;

import com.sun.jna.PointerType;
import com.sun.jna.Pointer;

/**
 * @author Kohsuke Kawaguchi
 */
public class zfs_handle_t extends PointerType {

    public boolean isNull() {
        return getPointer().equals(Pointer.NULL);
    }
}
