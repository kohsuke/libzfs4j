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
package com.sun;

import junit.framework.TestCase;
import org.jvnet.solaris.libzfs.ACLBuilder;
import org.jvnet.solaris.libzfs.LibZFS;
import org.jvnet.solaris.libzfs.ZFSFileSystem;
import org.jvnet.solaris.libzfs.ZFSObject;
import org.jvnet.solaris.libzfs.ZFSPool;
import org.jvnet.solaris.libzfs.ZFSType;
import org.jvnet.solaris.libzfs.ZFSPermission;
import org.jvnet.solaris.libzfs.jna.zfs_prop_t;
import org.jvnet.solaris.libzfs.jna.zpool_prop_t;

import java.util.EnumSet;
import java.util.Map;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {

    private static final String ZFS_TEST_POOL_OVERRIDE_PROPERTY = "libzfs.test.pool";

    private static final String ZFS_TEST_POOL_BASENAME_DEFAULT = "rpool/kohsuke/";

    private String ZFS_TEST_POOL_BASENAME;

    private final LibZFS zfs = new LibZFS();

    /**
     * The dataset name that can be created in a test.
     * This will be automatically destroyed at the end.
     */
    private String dataSet;

    public void setUp() throws Exception {
        super.setUp();

        /* allows override of zfs pool used in testing */
       ZFS_TEST_POOL_BASENAME = System
                .getProperty(ZFS_TEST_POOL_OVERRIDE_PROPERTY,
                        ZFS_TEST_POOL_BASENAME_DEFAULT);

        dataSet = ZFS_TEST_POOL_BASENAME + getName();

        assertFalse("Prerequisite Failed, DataSet already exists [" + dataSet+ "] ", zfs.exists(dataSet));
    }

    public void tearDown() throws Exception {
        super.tearDown();

        if (dataSet != null) {
            System.out.println("TearDown test dataset [" + dataSet + "]");

            if (zfs.exists(dataSet)) {
                final ZFSFileSystem fs = zfs.open(dataSet,ZFSFileSystem.class);
                fs.unshare();
                fs.unmount();
                fs.destory(true);
            }
        }
    }

    public void testApp() {
        System.out.println("Iterating roots");
        for (ZFSPool pool : zfs.roots()) {
            System.out.println(pool.getName());
            for (ZFSObject child : pool.descendants()) {
                System.out.println("- " + child.getName());
            }
        }
    }

    public void testGetFilesystemTree() {
        // List<ZFSPool> pools = zfs.roots();
        // if ( pools.size() > 0 ) {
        // ZFSObject filesystem = pools.get(0);
        ZFSObject filesystem = zfs.open("rpool");
        if (filesystem != null) {
            System.out.println("single tree: " + filesystem.getName());
            for (ZFSObject child : filesystem.children()) {
                if (child.getName().contains("@")) {
                    System.out.println("snapshot  :" + child.getName());
                } else {
                    System.out.println("child     :" + child.getName());
                }
            }
        } else {
            System.out.println("no zfs pools were found");
        }
    }

    public void testCreate() {
        ZFSObject fs = zfs.create(dataSet, ZFSFileSystem.class);

        assertNotNull("ZFSObject was null for DataSet [" + dataSet + "]",
                fs);
        assertEquals("ZFSObject doesn't match name specified at create",
                dataSet, fs.getName());
        assertTrue("ZFS exists doesn't report ZFS's creation", zfs
                .exists(dataSet));
    }

    public void testDestroy() {
        zfs.create(dataSet, ZFSFileSystem.class);

        assertTrue("Prerequisite Failed, Test DataSet [" + dataSet
                + "] didn't create", zfs.exists(dataSet));

        ZFSObject fs = zfs.open(dataSet);

        assertNotNull("ZFSObject was null for DataSet [" + dataSet + "]",
                fs);
        assertEquals("ZFSObject doesn't match name specified at open",
                dataSet, fs.getName());
        assertTrue("ZFS exists doesn't report ZFS", zfs.exists(dataSet));

        fs.destory();

        assertFalse("ZFS exists doesn't report ZFS as destroyed", zfs
                .exists(dataSet));
    }

    public void testUserProperty() {
        ZFSFileSystem o = zfs.create(dataSet,ZFSFileSystem.class);

        String property = "my:test";
        String time = String.valueOf(System.currentTimeMillis());
        o.setProperty(property, time);

        String v = o.getUserProperty(property);
        System.out.println("Property " + property + " is "+ v);
        assertEquals(v,time);
    }

    public void testGetZfsProperties() {
        for (ZFSPool pool : zfs.roots()) {
            System.out.println("pool    :" + pool.getName());

            Map<zfs_prop_t, String> zfsPoolProps = pool.getZfsProperty(EnumSet.allOf(zfs_prop_t.class));
            for (zfs_prop_t prop : zfsPoolProps.keySet()) {
                System.out.println("zfs_prop_t " + prop + "(" + prop.ordinal()
                        + ") = " + zfsPoolProps.get(prop));
            }
        }

        ZFSObject o = zfs.open("rpool/kohsuke");
        System.out.println("pool    :" + o.getName());

        Map<zfs_prop_t, String> zfsPoolProps = o.getZfsProperty(EnumSet.allOf(zfs_prop_t.class));
        for (zfs_prop_t prop : zfsPoolProps.keySet()) {
            System.out.println("zfs_prop_t " + prop + "(" + prop.ordinal()
                    + ") = " + zfsPoolProps.get(prop));
        }
    }

    public void testGetZpoolProperties() {
        for (ZFSPool o : zfs.roots()) {
            System.out.println("name:" + o.getName() + " size:"
                    + o.getZpoolProperty(zpool_prop_t.ZPOOL_PROP_SIZE)
                    + " used:"
                    + o.getZpoolProperty(zpool_prop_t.ZPOOL_PROP_USED));
        }
    }

    public void testAllow() {
        ZFSFileSystem fs = zfs.create(dataSet, ZFSFileSystem.class);
        ACLBuilder acl = new ACLBuilder();
        acl.everyone().with(ZFSPermission.CREATE);
        fs.allow(acl);
        fs.unallow(acl);
    }

    public void testInheritProperty() {
        ZFSFileSystem o  = zfs.create(dataSet, ZFSFileSystem.class);
        ZFSFileSystem o2 = zfs.create(dataSet+"/child",ZFSFileSystem.class);

        String property = "my:test";
        String time = String.valueOf(System.currentTimeMillis());
        o.setProperty(property, time);
        String v = o.getUserProperty(property);
        assertEquals(time,v);

        o2.inheritProperty(property);

        v = o2.getUserProperty(property);
        assertEquals(time,v);
    }

    public void test_zfsObject_exists() {
        final ZFSObject fs1 = zfs.create(dataSet, ZFSFileSystem.class);

        assertNotNull("Prerequisite Failed ZFS dataset created was null ["
                + dataSet + "]", fs1);

        assertTrue("ZFS exists failed for freshly created dataset", zfs
                .exists(dataSet));
        assertTrue("ZFS exists failed for freshly created dataset", zfs.exists(
                dataSet, ZFSType.FILESYSTEM));

        fs1.destory();
        assertFalse("ZFS exists failed for freshly destory dataset", zfs
                .exists(dataSet));
        assertFalse("ZFS exists failed for freshly destory dataset", zfs
                .exists(dataSet, ZFSType.FILESYSTEM));

        final ZFSObject fs2 = zfs.create(dataSet, ZFSFileSystem.class);

        assertNotNull("Prerequisite Failed ZFS dataset created was null ["
                + dataSet + "]", fs2);

        assertTrue("ZFS exists failed for freshly created dataset", zfs
                .exists(dataSet));
        assertTrue("ZFS exists failed for freshly created dataset", zfs.exists(
                dataSet, ZFSType.FILESYSTEM));

        fs2.destory();
        assertFalse("ZFS exists failed for freshly destory dataset", zfs
                .exists(dataSet));
        assertFalse("ZFS exists failed for freshly destory dataset", zfs
                .exists(dataSet, ZFSType.FILESYSTEM));
    }

    public void test_zfsObject_isMounted() {
        final ZFSFileSystem fs = zfs.create(dataSet, ZFSFileSystem.class);

        assertNotNull("Prerequisite Failed ZFS dataset created was null ["
                + dataSet + "]", fs);

        assertFalse("ZFS spec does not have dataset mounted at create", fs
                .isMounted());

        fs.mount();
        assertTrue("ZFS dataset mount failed, or isMounted failed", fs
                .isMounted());

        fs.unmount();
        assertFalse("ZFS dataset unmount failed, or isMounted failed", fs
                .isMounted());

        fs.mount();
        assertTrue("ZFS dataset mount failed, or isMounted failed", fs
                .isMounted());

        fs.unmount();
        assertFalse("ZFS dataset unmount failed, or isMounted failed", fs
                .isMounted());
    }

    public void xtest_zfsObject_isShared() {
        final ZFSFileSystem fs = zfs.create(dataSet, ZFSFileSystem.class);

        assertNotNull("Prerequisite Failed ZFS dataset created was null ["
                + dataSet + "]", fs);

        assertFalse("ZFS spec does not have dataset shared at create", fs
                .isShared());

        fs.share();
        assertTrue("ZFS dataset share failed, or isShared failed", fs
                .isShared());

        fs.unshare();
        assertFalse("ZFS dataset unshare failed, or isShared failed", fs
                .isShared());

        fs.share();
        assertTrue("ZFS dataset share failed, or isShared failed", fs
                .isShared());

        fs.unshare();
        assertFalse("ZFS dataset unshare failed, or isShared failed", fs
                .isShared());
    }

}
