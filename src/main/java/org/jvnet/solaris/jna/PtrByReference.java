package org.jvnet.solaris.jna;

import com.sun.jna.PointerType;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ByReference;

/**
 * {@link PointerByReference} equivalent but type-safe.
 * @author Kohsuke Kawaguchi
 */
public class PtrByReference<T extends PointerType> extends ByReference {
    public PtrByReference() {
        this(null);
    }

    public PtrByReference(T value) {
        super(Pointer.SIZE);
        setValue(value);
    }

    public void setValue(T value) {
        getPointer().setPointer(0, value==null ? null : value.getPointer() );
    }

    public T getValue(Class<T> type) {
        Pointer value = getPointer().getPointer(0);
        if(value==null)     return null;

        try {
            T v = type.newInstance();
            v.setPointer(value);
            return v;
        } catch (InstantiationException e) {
            throw new InstantiationError(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
}
