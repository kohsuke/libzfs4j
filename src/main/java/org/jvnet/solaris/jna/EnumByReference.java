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
