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
