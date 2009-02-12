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

import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Arrays;
import java.util.Collection;

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
    private final Set<PermissionBuilder> builders = new HashSet<PermissionBuilder>();

    public abstract class PermissionBuilder {
        protected char inheritanceBits=0;

        private final Set<ZFSPermission> permissions = EnumSet.noneOf(ZFSPermission.class);

        private PermissionBuilder() {
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

        protected abstract String format(char ch);

        private void toNativeFormat(nvlist_t nv) {
            for(int i=0; i<3; i++) {
                if((inheritanceBits&(1<<i))!=0) {
                    nvlist_t perms = nvlist_t.allocMap();
                    for (ZFSPermission p : permissions)
                        perms.put(p.name().toLowerCase(),true);
                    nv.put(format(((char)(1<<i))),perms);
                }
            }
        }
    }

    /**
     * For everyone.
     */
    public PermissionBuilder everyone() {
        return add(new PermissionBuilder() {
            protected String format(char ch) {
                return "e"+ch+"$";
            }
        });
    }

    /**
     * For user
     */
    public PermissionBuilder user(final int uid) {
        return add(new PermissionBuilder() {
            protected String format(char ch) {
                return "u"+ch+"$"+String.valueOf(uid);
            }
        });
    }

    /**
     * For group
     */
    public PermissionBuilder group(final int gid) {
        return add(new PermissionBuilder() {
            protected String format(char ch) {
                return "g"+ch+"$"+String.valueOf(gid);
            }
        });
    }

    private PermissionBuilder add(PermissionBuilder pb) {
        builders.add(pb);
        return pb;
    }

    /**
     * Builds up the native nvlist format to be passed to libzfs.
     */
    protected nvlist_t toNativeFormat() {
        nvlist_t nv = nvlist_t.allocMap();
        for (PermissionBuilder builder : builders)
            builder.toNativeFormat(nv);
        return nv;
    }
}
