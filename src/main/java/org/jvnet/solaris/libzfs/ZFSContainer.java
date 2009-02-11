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
package org.jvnet.solaris.libzfs;

import java.util.List;

/**
 * Object that contains {@link ZFSObject}s.
 *
 * @author Kohsuke Kawaguchi
 */
public interface ZFSContainer {
    /**
     * List the children of this ZFS object (but not recursively.)
     *
     * @return
     *      Never null. Sorted by the in-order of the traversal.
     */
    public List<? extends ZFSObject> children();

    /**
     * List the specific kind of children of this ZFS object (but not recursively.)
     *
     * @return
     *      Never null. Sorted by the in-order of the traversal.
     */
    public <T extends ZFSObject> List<T> children(Class<T> type);

    /**
     * List the children of this ZFS object recursively, excluding the 'this' object itself.
     *
     * @return
     *      Never null. Sorted by the in-order of the traversal.
     */
    public List<ZFSObject> descendants();

    public <T extends ZFSObject> List<T> descendants(Class<T> type);
}
