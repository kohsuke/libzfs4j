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
package org.jvnet.solaris.libzfs.jna;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * @author Kohsuke Kawaguchi
 */
public class zprop_list_t extends Structure implements Structure.ByReference {
    /**
     * If this property is a native property, its ID.
     * Otherwise ZPROP_INVAL.
     */
    public int pl_prop;
    /**
     * If this property is an user-defined property, its name.
     */
    public String pl_user_prop;
    /**
     * Next in the property list.
     */
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
