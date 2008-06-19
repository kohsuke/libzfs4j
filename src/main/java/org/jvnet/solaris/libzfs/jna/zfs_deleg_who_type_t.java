package org.jvnet.solaris.libzfs.jna;

/**
 * Definitions for the Delegation.
 */
public enum zfs_deleg_who_type_t {
    ZFS_DELEG_WHO_UNKNOWN('\0'),
    ZFS_DELEG_USER('u'),
    ZFS_DELEG_USER_SETS('U'),
    ZFS_DELEG_GROUP('g'),
    ZFS_DELEG_GROUP_SETS('G'),
    ZFS_DELEG_EVERYONE('e'),
    ZFS_DELEG_EVERYONE_SETS('E'),
    ZFS_DELEG_CREATE('c'),
    ZFS_DELEG_CREATE_SETS('C'),
    ZFS_DELEG_NAMED_SET('s'),
    ZFS_DELEG_NAMED_SET_SETS('S');

    zfs_deleg_who_type_t(char ch) {
        this.code = ch;
    }

    public final char code;
}