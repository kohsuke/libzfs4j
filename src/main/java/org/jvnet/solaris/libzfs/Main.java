package org.jvnet.solaris.libzfs;

/**
 * A test program.
 *
 * @author Kohsuke Kawaguchi
 */
public class Main {
    public static void main(String[] args) {
        LibZFS zfs;
        try {
            zfs = new LibZFS();
        } catch (Throwable e) {
            System.out.println("Aborted because " + e.toString());
            return;
        }
        for (ZFSFileSystem fs : zfs.roots()) {
            System.out.println(fs.getName());
            for (ZFSFileSystem c : fs.children(ZFSFileSystem.class)) {
                System.out.println(c.getName());
            }
        }
    }
}
