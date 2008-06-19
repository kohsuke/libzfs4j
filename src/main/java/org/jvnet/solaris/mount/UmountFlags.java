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

package org.jvnet.solaris.mount;

/**
 * Flag bits passed to mount(2).
 */
public class UmountFlags {
    /**
     * Read-only
     */
    public static final int MS_RDONLY = 0x0001;
    /**
     * Old (4-argument) mount (compatibility)
     */
    public static final int MS_FSS = 0x0002;
    /**
     * 6-argument mount
     */
    public static final int MS_DATA = 0x0004;
    /**
     * Setuid programs disallowed
     */
    public static final int MS_NOSUID = 0x0010;
    /**
     * Remount
     */
    public static final int MS_REMOUNT = 0x0020;
    /**
     * Return ENAMETOOLONG for long filenames
     */
    public static final int MS_NOTRUNC = 0x0040;
    /**
     * Allow overlay mounts
     */
    public static final int MS_OVERLAY = 0x0080;
    /**
     * Data is a an in/out option string
     */
    public static final int MS_OPTIONSTR = 0x0100;
    /**
     * Clustering: Mount into global name space
     */
    public static final int MS_GLOBAL = 0x0200;
    /**
     * Forced unmount
     */
    public static final int MS_FORCE = 0x0400;
    /**
     * Don't show mount in mnttab
     */
    public static final int MS_NOMNTTAB = 0x0800;
}
