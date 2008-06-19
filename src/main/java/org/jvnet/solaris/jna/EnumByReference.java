package org.jvnet.solaris.jna;

import com.sun.jna.ptr.ByReference;

/**
 * @author Kohsuke Kawaguchi
 */
public class EnumByReference<T extends Enum<T>> extends ByReference {
    public EnumByReference() {
        this(null);
    }

    public EnumByReference(T value) {
        super(4);
        setValue(value);
    }

    public void setValue(T value) {
        getPointer().setInt(0L, value==null ? 0 : value.ordinal());
    }

    public T getValue(Class<T> type) {
        return type.getEnumConstants()[getPointer().getInt(0L)];
    }
}
