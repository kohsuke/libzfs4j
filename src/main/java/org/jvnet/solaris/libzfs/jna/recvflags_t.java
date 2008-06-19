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
