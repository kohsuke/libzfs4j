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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.List;
import junit.framework.TestCase;
import org.jvnet.solaris.libzfs.LibZFS;
import org.jvnet.solaris.libzfs.ZFSObject;
import org.jvnet.solaris.libzfs.ZFSPool;
import org.jvnet.solaris.libzfs.ZFSType;
import org.jvnet.solaris.libzfs.jna.zfs_prop_t;
import org.jvnet.solaris.libzfs.jna.zpool_prop_t;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    
    public void testApp()
    {
        LibZFS zfs = new LibZFS();

        System.out.println("Iterating roots");
        for (ZFSPool pool : zfs.roots()) {
            System.out.println(pool.getName());
            for (ZFSObject child : pool.children()) {
                System.out.println("- "+child.getName());
            }
        }
    }
 
    public void testGetFilesystemTree() {
      LibZFS zfs = new LibZFS();
      //List<ZFSPool> pools = zfs.roots();
      //if ( pools.size() > 0 ) {
       // ZFSObject filesystem = pools.get(0);
       ZFSObject filesystem = zfs.open("rpool");
       if ( filesystem != null ) {
        System.out.println("single tree: "+filesystem.getName());
        List<ZFSObject> clist = new ArrayList<ZFSObject>();
        for(ZFSObject child : filesystem.children(clist,filesystem)) {
          if ( child.getName().contains("@")) {
            System.out.println("snapshot  :"+child.getName());  
          }
          else {
            System.out.println("child     :"+child.getName());
          }
        }
      }
      else {
        System.out.println("no zfs pools were found");      
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
        o = zfs.open("rpool/kohsuke");
        o.unmount();
        o.destory();
    }

    public void testUserProperty() {
        LibZFS zfs = new LibZFS();
        ZFSObject o = zfs.open("rpool");
        String property = "my:test";
        o.setProperty(property, String.valueOf(System.currentTimeMillis()));
        System.out.println("Property "+property+" is "+o.getUserProperty(property));
    }
    
    public void testGetZfsProperties() {
        LibZFS zfs = new LibZFS();
        for (ZFSPool pool : zfs.roots()) {
            System.out.println("pool    :"+pool.getName());

            ArrayList<zfs_prop_t> list = new ArrayList<zfs_prop_t>();
            for (zfs_prop_t prop : EnumSet.allOf(zfs_prop_t.class) ) {
              list.add(prop);
            }
        
            Hashtable<zfs_prop_t,String> map = pool.getZfsProperty(list);
            for( zfs_prop_t prop : map.keySet() ) {
              System.out.println("zfs_prop_t "+prop+"("+prop.ordinal()+") = "+map.get(prop));
            }
        }
    }

    public void testGetZpoolProperties() {
      LibZFS zfs = new LibZFS();
      
      for (ZFSPool pool : zfs.roots() ) {
        ZFSObject o = zfs.open(pool.getName());
        System.out.println("name:"+o.getName()+
          " size:"+o.getZpoolProperty(zpool_prop_t.ZPOOL_PROP_SIZE)+
          " used:"+o.getZpoolProperty(zpool_prop_t.ZPOOL_PROP_USED));
      }
    }
    
    public void testInheritProperty() {
      LibZFS zfs = new LibZFS();
     
      ZFSObject o = zfs.open("rpool");
      String property = "my:test";
      o.setProperty(property, String.valueOf(System.currentTimeMillis()));
      System.out.println("set test: Property "+property+" is "+o.getUserProperty(property));
      o.inheritProperty(property);
      
      ZFSObject o2 = zfs.open("rpool");
      System.out.println("inherit test: Property "+property+" is "+o2.getUserProperty(property));
    }
}
