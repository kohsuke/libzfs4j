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

/**
 * ZFS error code.
 * 
 * @author Kohsuke Kawaguchi
 */
public enum ErrorCode {

    EZFS_NOMEM, /* out of memory */
    EZFS_BADPROP, /* invalid property value */
    EZFS_PROPREADONLY, /* cannot set readonly property */
    EZFS_PROPTYPE, /* property does not apply to dataset type */
    EZFS_PROPNONINHERIT, /* property is not inheritable */
    EZFS_PROPSPACE, /* bad quota or reservation */
    EZFS_BADTYPE, /* dataset is not of appropriate type */
    EZFS_BUSY, /* pool or dataset is busy */
    EZFS_EXISTS, /* pool or dataset already exists */
    EZFS_NOENT, /* no such pool or dataset */
    EZFS_BADSTREAM, /* bad backup stream */
    EZFS_DSREADONLY, /* dataset is readonly */
    EZFS_VOLTOOBIG, /* volume is too large for 32-bit system */
    EZFS_VOLHASDATA, /* volume already contains data */
    EZFS_INVALIDNAME, /* invalid dataset name */
    EZFS_BADRESTORE, /* unable to restore to destination */
    EZFS_BADBACKUP, /* backup failed */
    EZFS_BADTARGET, /* bad attach/detach/replace target */
    EZFS_NODEVICE, /* no such device in pool */
    EZFS_BADDEV, /* invalid device to add */
    EZFS_NOREPLICAS, /* no valid replicas */
    EZFS_RESILVERING, /* currently resilvering */
    EZFS_BADVERSION, /* unsupported version */
    EZFS_POOLUNAVAIL, /* pool is currently unavailable */
    EZFS_DEVOVERFLOW, /* too many devices in one vdev */
    EZFS_BADPATH, /* must be an absolute path */
    EZFS_CROSSTARGET, /* rename or clone across pool or dataset */
    EZFS_ZONED, /* used improperly in local zone */
    EZFS_MOUNTFAILED, /* failed to mount dataset */
    EZFS_UMOUNTFAILED, /* failed to unmount dataset */
    EZFS_UNSHARENFSFAILED, /* unshare(1M) failed */
    EZFS_SHARENFSFAILED, /* share(1M) failed */
    EZFS_DEVLINKS, /* failed to create zvol links */
    EZFS_PERM, /* permission denied */
    EZFS_NOSPC, /* out of space */
    EZFS_IO, /* I/O error */
    EZFS_INTR, /* signal received */
    EZFS_ISSPARE, /* device is a hot spare */
    EZFS_INVALCONFIG, /* invalid vdev configuration */
    EZFS_RECURSIVE, /* recursive dependency */
    EZFS_NOHISTORY, /* no history object */
    EZFS_UNSHAREISCSIFAILED, /* iscsitgtd failed request to unshare */
    EZFS_SHAREISCSIFAILED, /* iscsitgtd failed request to share */
    EZFS_POOLPROPS, /* couldn't retrieve pool props */
    EZFS_POOL_NOTSUP, /* ops not supported for this type of pool */
    EZFS_POOL_INVALARG, /* invalid argument for this pool operation */
    EZFS_NAMETOOLONG, /* dataset name is too long */
    EZFS_OPENFAILED, /* open of device failed */
    EZFS_NOCAP, /* couldn't get capacity */
    EZFS_LABELFAILED, /* write of label failed */
    EZFS_ISCSISVCUNAVAIL, /* iscsi service unavailable */
    EZFS_BADWHO, /* invalid permission who */
    EZFS_BADPERM, /* invalid permission */
    EZFS_BADPERMSET, /* invalid permission set name */
    EZFS_NODELEGATION, /* delegated administration is disabled */
    EZFS_PERMRDONLY, /* pemissions are readonly */
    EZFS_UNSHARESMBFAILED, /* failed to unshare over smb */
    EZFS_SHARESMBFAILED, /* failed to share over smb */
    EZFS_BADCACHE, /* bad cache file */
    EZFS_ISL2CACHE, /* device is for the level 2 ARC */
    EZFS_VDEVNOTSUP, /* unsupported vdev type */
    EZFS_UNKNOWN;

    /**
     * Error code number.
     */
    public int code() {
        return ordinal() + 2000;
    }

    public static ErrorCode fromCode(final int c) {
        try {
            return ErrorCode.values()[c - 2000];
        } catch (ArrayIndexOutOfBoundsException e) {
            return EZFS_UNKNOWN;
        }
    }

}
