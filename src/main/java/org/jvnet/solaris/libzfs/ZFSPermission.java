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
 * @author Kohsuke Kawaguchi
 */
public enum ZFSPermission {
    ALLOW, CLONE, CREATE, DESTROY, MOUNT, PROMOTE, RECEIVE, RENAME, ROLLBACK, SNAPSHOT,
    SHARE, SEND,
    // permissions
    ACLINHERIT,
    ACLMODE, ATIME, CANMOUNT, CHECKSUM, COMPRESSION, COPIES, DEVICES, EXEC,
    MOUNTPOINT, PRIMARYCACHE, QUOTA, READONLY, RECORDSIZE, RESERVATION,
    SECONDARYCACHE, SETUID, SHAREISCSI, SHARENFS, SNAPDIR, VERSION, VOLSIZE,
    XATTR, ZONED, USERPROP
}
