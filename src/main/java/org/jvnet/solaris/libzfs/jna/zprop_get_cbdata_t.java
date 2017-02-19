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
public class zprop_get_cbdata_t {
    int cb_sources;
    int[] cb_columns = new int[4];
    int[] cb_colwidths = new int[5];
    boolean cb_scripted;
    boolean cb_literal;
    boolean cb_first;
    zprop_list_t cb_proplist;
    zfs_type_t cb_type;
}
