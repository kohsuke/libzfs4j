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
