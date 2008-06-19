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
import org.jvnet.solaris.libzfs.LibZFS;
import org.jvnet.solaris.libzfs.ZFSObject;
import org.jvnet.solaris.libzfs.ZFSPool;
import org.jvnet.solaris.libzfs.ZFSType;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    public void testApp()
    {
        LibZFS zfs = new LibZFS();
//        PointerByReference props = new PointerByReference();
//        if(zfs.zprop_get_list(handle,"name,used,available,referenced,mountpoint",props, zfs_type_t.DATASET)!=0)
//            fail();

//        zprop_list_t prop = new zprop_list_t(props.getValue());
//        prop.read();
//
//        zfs.zprop_free_list(prop);

        System.out.println("Iterating roots");
        for (ZFSPool pool : zfs.roots()) {
            System.out.println(pool.getName());
            for (ZFSObject child : pool.children()) {
                System.out.println("- "+child.getName());
            }
        }
    }

    public void testCreate() {
        LibZFS zfs = new LibZFS();
        ZFSObject o = zfs.create("rpool/kohsuke/test", ZFSType.FILESYSTEM);
        o.mount();
        o.share();
    }

    public void testDestroy() {
        LibZFS zfs = new LibZFS();
        ZFSObject o = zfs.open("rpool/kohsuke/test");
        o.unmount();
        o.destory();
    }
}
