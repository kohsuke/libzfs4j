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

import org.jvnet.solaris.nvlist.jna.nvlist_t;
import org.jvnet.solaris.libzfs.jna.libzfs;
import org.jvnet.solaris.libzfs.jna.zfs_deleg_who_type_t;
import org.jvnet.solaris.jna.PtrByReference;

import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Arrays;
import java.util.Collection;

import com.sun.jna.ptr.PointerByReference;

/**
 * Access control list for ZFS dataset.
 *
 * <h2>Usage</h2>
 * <ol>
 *  <li>Create a fresh ACL.
 *  <li>Call methods on this object to build up (who,permissions) list
 *  <li>Call {@link ZFSObject#allow(ACLBuilder)} or {@link ZFSObject#unallow(ACLBuilder)} to add or remove
 *      those permissions from a dataset.
 * </ol>
 *
 *
 * @author Kohsuke Kawaguchi
 */
public class ACLBuilder {
    /*package*/ final Set<PermissionBuilder> builders = new HashSet<PermissionBuilder>();

    public class PermissionBuilder {
        private final zfs_deleg_who_type_t whoType;
        protected char inheritanceBits=0;

        private final Set<ZFSPermission> permissions = EnumSet.noneOf(ZFSPermission.class);

        private PermissionBuilder(zfs_deleg_who_type_t whoType) {
            this.whoType = whoType;
        }

        /**
         * Specifies that this entry applies to this dataset.
         */
        public PermissionBuilder onThisDataset() {
            inheritanceBits |= 1;
            return this;
        }

        /**
         * Specifies that this entry applies to children and their descendants of this dataset.
         */
        public PermissionBuilder onDescendants() {
            inheritanceBits |= 2;
            return this;
        }

        /**
         * Specifies that this entry applies to the new datasets created under this dataset.
         */
        public PermissionBuilder onNewDataset() {
            inheritanceBits |= 4;
            return this;
        }

        public PermissionBuilder with(ZFSPermission... args) {
            return with(Arrays.asList(args));
        }

        public PermissionBuilder with(ZFSPermission arg) {
            permissions.add(arg);
            return this;
        }

        public PermissionBuilder with(Collection<ZFSPermission> arg) {
            permissions.addAll(arg);
            return this;
        }

        public PermissionBuilder withEverything() {
            return with(EnumSet.allOf(ZFSPermission.class));
        }

        protected String who() { return null; }

        protected nvlist_t toNativeFormat(ZFSObject dataset) {
            StringBuilder buf = new StringBuilder();
            for (ZFSPermission p : permissions) {
                if(buf.length()>0)  buf.append(',');
                buf.append(p);
            }

            // if none is specified, assume it's on this dataset and its descendants.
            // this is consistent with zfs CLI.
            if(inheritanceBits==0)
                inheritanceBits = 3;

            PtrByReference<nvlist_t> r = new PtrByReference<nvlist_t>();
            if(libzfs.LIBZFS.zfs_build_perms(dataset.handle,
                    who(), buf.toString().toLowerCase(), whoType.code, inheritanceBits, r)!=0)
                throw new ZFSException(dataset.library);

            return r.getValue(nvlist_t.class);
        }
    }

    /**
     * For everyone.
     */
    public PermissionBuilder everyone() {
        return add(new PermissionBuilder(zfs_deleg_who_type_t.ZFS_DELEG_EVERYONE));
    }

    /**
     * For user
     */
    public PermissionBuilder user(final String userName) {
        return add(new PermissionBuilder(zfs_deleg_who_type_t.ZFS_DELEG_USER) {
            protected String who() {
                return userName;
            }
        });
    }

    /**
     * For group
     */
    public PermissionBuilder group(final String groupName) {
        return add(new PermissionBuilder(zfs_deleg_who_type_t.ZFS_DELEG_GROUP) {
            protected String who() {
                return groupName;
            }
        });
    }

    private PermissionBuilder add(PermissionBuilder pb) {
        builders.add(pb);
        return pb;
    }
}
