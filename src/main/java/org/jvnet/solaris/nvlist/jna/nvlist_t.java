package org.jvnet.solaris.nvlist.jna;

import com.sun.jna.PointerType;
import org.jvnet.solaris.jna.PtrByReference;
import static org.jvnet.solaris.nvlist.jna.libnvpair.LIBNVPAIR;

/**
 * Opaque handle type that represents name/value pair list.
 * @author Kohsuke Kawaguchi
 */
public class nvlist_t extends PointerType {
    private boolean disposed;
    /**
     * Allocates a new {@link nvlist_t}.
     */
    public static nvlist_t alloc(int nvflag) {
        PtrByReference<nvlist_t> buf = new PtrByReference<nvlist_t>();
        if(LIBNVPAIR.nvlist_alloc(buf,nvflag,0)!=0)
            throw new NVListException();
        return buf.getValue(nvlist_t.class);
    }

    public void put(String key, String value) {
        if(LIBNVPAIR.nvlist_add_string(this,key,value)!=0)
            throw new NVListException();
    }

    @Override
    protected void finalize() throws Throwable {
        dispose();
        super.finalize();
    }

    private synchronized void dispose() {
        if(!disposed)
            LIBNVPAIR.nvlist_free(this);
        disposed = true;
    }
}
