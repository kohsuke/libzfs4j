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
