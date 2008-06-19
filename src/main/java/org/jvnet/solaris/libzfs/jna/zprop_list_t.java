package org.jvnet.solaris.libzfs.jna;

import com.sun.jna.NativeLong;
import com.sun.jna.Structure;
import com.sun.jna.Pointer;

/**
 * @author Kohsuke Kawaguchi
 */
public class zprop_list_t extends Structure implements Structure.ByReference {
    public int pl_prop;
    public String pl_user_prop;
    public zprop_list_t pl_next;
    public boolean pl_all;
    public NativeLong pl_width;
    public boolean pl_fixed;

    public zprop_list_t() {
    }

    public zprop_list_t(Pointer p) {
        useMemory(p);
    }
}
