package org.jvnet.solaris.jna;

import com.sun.jna.ptr.ByReference;

/**
 * @author Kohsuke Kawaguchi
 */
public class BooleanByReference extends ByReference {

    public BooleanByReference() {
        this(false);
    }

    public BooleanByReference(boolean value) {
        super(4);
        setValue(value);
    }

    public void setValue(boolean value) {
        getPointer().setInt(0L, value ? 1 : 0);
    }

    public boolean getValue() {
        return getPointer().getInt(0L) != 0;
    }
}
