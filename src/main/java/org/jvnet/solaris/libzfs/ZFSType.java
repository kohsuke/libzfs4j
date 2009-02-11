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
    FILESYSTEM(1,ZFSFileSystem.class),
    /**
     * A read-only version of a file  system  or  volume  at  a
     * given  point in time. It is specified as filesystem@name
     * or volume@name.
     */
    SNAPSHOT(2,ZFSSnapshot.class),
    /**
     * A logical volume exported as a raw or block device. This
     * type  of  dataset should only be used under special cir-
     * cumstances. File systems  are  typically  used  in  most
     * environments.  Volumes  cannot  be  used in a non-global
     * zone.
     */
    VOLUME(4,ZFSVolume.class),
    /**
     * Pool is a storage abstraction in which filesysems, snapshots,
     * and volumes are stored.
     *
     * TODO: find an official documentation and replace this.
     */
    POOL(8,ZFSPool.class);

    ZFSType(int code, Class<? extends ZFSObject> type) {
        this.code = code;
        this.type = type;
    }

    public final int code;

    public final Class<? extends ZFSObject> type;


    /*package*/ static ZFSType fromCode(int n) {
        for( ZFSType t : ZFSType.class.getEnumConstants() )
            if(t.code==n)
                return t;
        return null;
    }

    /*package*/ static ZFSType fromType(Class<? extends ZFSObject> subType) {
        for( ZFSType t : ZFSType.class.getEnumConstants() )
            if(t.type==subType)
                return t;
        return null;
    }
}
