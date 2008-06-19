package org.jvnet.solaris.libzfs.jna;

public enum zfs_deleg_inherit_t {
	ZFS_DELEG_NONE,
	ZFS_DELEG_PERM_LOCAL,
	ZFS_DELEG_PERM_DESCENDENT,
	ZFS_DELEG_PERM_LOCALDESCENDENT,
	ZFS_DELEG_PERM_CREATE
}
