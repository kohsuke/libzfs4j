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

/**
 * Definitions for the Delegation.
 */
public enum zfs_deleg_who_type_t {
    ZFS_DELEG_WHO_UNKNOWN('\0'),
    ZFS_DELEG_USER('u'),
    ZFS_DELEG_USER_SETS('U'),
    ZFS_DELEG_GROUP('g'),
    ZFS_DELEG_GROUP_SETS('G'),
    ZFS_DELEG_EVERYONE('e'),
    ZFS_DELEG_EVERYONE_SETS('E'),
    ZFS_DELEG_CREATE('c'),
    ZFS_DELEG_CREATE_SETS('C'),
    ZFS_DELEG_NAMED_SET('s'),
    ZFS_DELEG_NAMED_SET_SETS('S');

    zfs_deleg_who_type_t(char ch) {
        this.code = ch;
    }

    public final char code;
}
