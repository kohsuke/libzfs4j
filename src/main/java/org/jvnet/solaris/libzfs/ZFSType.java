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

import org.jvnet.solaris.libzfs.jna.zfs_type_t;

/**
 * Type of {@link ZFSObject}.
 *
 * @author Kohsuke Kawaguchi
 * @see zfs_type_t
 */
public enum ZFSType {
    /**
     * A ZFS dataset of type "filesystem" that can  be  mounted
     * within  the  standard  system namespace and behaves like
     * other file systems. While ZFS file systems are  designed
     * to  be  POSIX compliant, known issues exist that prevent
     * compliance in some cases. Applications  that  depend  on
     * standards  conformance  might  fail  due  to nonstandard
     * behavior when checking file system free space.
     */
    FILESYSTEM(1),
    /**
     * A read-only version of a file  system  or  volume  at  a
     * given  point in time. It is specified as filesystem@name
     * or volume@name.
     */
    SNAPSHOT(2),
    /**
     * A logical volume exported as a raw or block device. This
     * type  of  dataset should only be used under special cir-
     * cumstances. File systems  are  typically  used  in  most
     * environments.  Volumes  cannot  be  used in a non-global
     * zone.
     */
    VOLUME(4),
    /**
     * Pool is a storage abstraction in which filesysems, snapshots,
     * and volumes are stored.
     *
     * TODO: find an official documentation and replace this.
     */
    POOL(8);

    ZFSType(final int code) {
        this.code = code;
    }

    public final int code;

}
