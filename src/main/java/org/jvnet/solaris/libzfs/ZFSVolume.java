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

import static org.jvnet.solaris.libzfs.jna.libzfs.LIBZFS;

import org.jvnet.solaris.libzfs.jna.zfs_handle_t;

/**
 * @author Kohsuke Kawaguchi
 */
public final class ZFSVolume extends ZFSObject {
    /*package*/ ZFSVolume(LibZFS parent, zfs_handle_t handle) {
        super(parent, handle);
    }


    /**
     * Share this dataset.
     */
    public void shareISCSI() {
        if (LIBZFS.zfs_share_iscsi(handle) != 0) {
            throw new ZFSException(library);
        }
    }

    /**
     * Unshare this dataset.
     */
    public void unshareISCSI() {
        if (LIBZFS.zfs_unshare_iscsi(handle) != 0) {
            throw new ZFSException(library);
        }
    }

}
