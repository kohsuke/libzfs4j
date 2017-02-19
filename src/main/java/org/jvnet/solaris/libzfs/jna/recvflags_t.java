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
 * @author Kohsuke Kawaguchi
 */
public abstract class recvflags_t {
    private recvflags_t() {}

    // verify the packing rule
    /**
     * print informational messages (ie, -v was specified)
     */
    public static final byte verbose = (byte)0x80;

    /**
     * the destination is a prefix, not the exact fs (ie, -d)
     */
    public static final byte isprefix = (byte)0x40;

    /**
     * do not actually do the recv, just check if it would work (ie, -n)
     */
    public static final byte dryrun = (byte)0x20;

    /**
     * rollback/destroy filesystems as necessary (eg, -F)
     */
    public static final byte force = (byte)0x10;

    /**
     * set "canmount=off" on all modified filesystems
     */
    public static final byte canmountoff = (byte)0x08;

    /**
     * byteswap flag is used internally; callers need not specify
     */
    public static final byte byteswap = (byte)0x04;
}
