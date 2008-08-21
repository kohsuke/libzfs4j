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

package org.jvnet.solaris.libzfs.jna;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;
import org.jvnet.solaris.avl.avl_node_t;
import org.jvnet.solaris.avl.avl_tree_t;
import org.jvnet.solaris.jna.BooleanByReference;
import org.jvnet.solaris.jna.EnumByReference;
import org.jvnet.solaris.nvlist.jna.nvlist_t;
import org.jvnet.solaris.mount.UmountFlags;

/**
 * @author Kohsuke Kawaguchi
 */
public interface libzfs extends Library {
    public static final libzfs LIBZFS = (libzfs) Native.loadLibrary("zfs",libzfs.class);

/*
 * Miscellaneous ZFS constants
 */
    public static final int MAXNAMELEN = 256;
    public static final int MAXPATHLEN = 1024;
    public static final int	ZFS_MAXNAMELEN		=MAXNAMELEN;
    public static final int	ZPOOL_MAXNAMELEN	=MAXNAMELEN;
    public static final int	ZFS_MAXPROPLEN		=MAXPATHLEN;
    public static final int	ZPOOL_MAXPROPLEN	=MAXPATHLEN;

    /*
    * The following data structures are all part
    * of the zfs_allow_t data structure which is
    * used for printing 'allow' permissions.
    * It is a linked list of zfs_allow_t's which
    * then contain avl tree's for user/group/sets/...
    * and each one of the entries in those trees have
    * avl tree's for the permissions they belong to and
    * whether they are local,descendent or local+descendent
    * permissions.  The AVL trees are used primarily for
    * sorting purposes, but also so that we can quickly find
    * a given user and or permission.
    */
class zfs_perm_node_t extends Structure implements Structure.ByReference {
	avl_node_t z_node;
	char[] z_pname = new char[MAXPATHLEN];
}

class zfs_allow_node_t extends Structure implements Structure.ByReference {
	avl_node_t z_node;
	char[] z_key = new char[MAXPATHLEN];		/* name, such as joe */
	avl_tree_t z_localdescend;	/* local+descendent perms */
	avl_tree_t z_local;		/* local permissions */
	avl_tree_t z_descend;		/* descendent permissions */
    // TODO: KK: aren't there avl_tree_t pointers?
}

class zfs_allow_t extends Structure implements Structure.ByReference {
	zfs_allow_t z_next;
	char[] z_setpoint = new char[MAXPATHLEN];
	avl_tree_t z_sets;
	avl_tree_t z_crperms;
	avl_tree_t z_user;
	avl_tree_t z_group;
	avl_tree_t z_everyone;
}

/*
 * Library initialization
 */
libzfs_handle_t libzfs_init();
void libzfs_fini(libzfs_handle_t handle);

libzfs_handle_t zpool_get_handle(zpool_handle_t handle);
libzfs_handle_t zfs_get_handle(zfs_handle_t handle);

void libzfs_print_on_error(libzfs_handle_t lib, boolean flag);

int libzfs_errno(libzfs_handle_t lib);
String libzfs_error_action(libzfs_handle_t lib);
String libzfs_error_description(libzfs_handle_t lib);

/*
 * Basic handle functions
 */
zpool_handle_t zpool_open(libzfs_handle_t lib, String name);
zpool_handle_t zpool_open_canfail(libzfs_handle_t lib, String name);
void zpool_close(zpool_handle_t pool);
String zpool_get_name(zpool_handle_t pool);
int zpool_get_state(zpool_handle_t pool);
String zpool_state_to_name(vdev_state_t _1, vdev_aux_t _2);

/*
 * Iterate over all active pools in the system.
 */
interface zpool_iter_f extends Callback {
    int callback(zpool_handle_t handle, Pointer arg);
}
int zpool_iter(libzfs_handle_t lib, zpool_iter_f callback, Pointer arg);

/*
 * Functions to create and destroy pools
 */
int zpool_create(libzfs_handle_t lib, String poolName, nvlist_t nvroot, nvlist_t props);
int zpool_destroy(zpool_handle_t pool);
int zpool_add(zpool_handle_t pool, nvlist_t _1);

/*
 * Functions to manipulate pool and vdev state
 */
int zpool_scrub(zpool_handle_t pool, pool_scrub_type_t scrub);
int zpool_clear(zpool_handle_t pool, String name);

int zpool_vdev_online(zpool_handle_t pool, String _1, int _2, vdev_state_t _3);
int zpool_vdev_offline(zpool_handle_t pool, String _2, boolean _3);
int zpool_vdev_attach(zpool_handle_t pool, String _2, String _3, nvlist_t _4, int _5);
int zpool_vdev_detach(zpool_handle_t pool, String _2);
int zpool_vdev_remove(zpool_handle_t pool, String _2);

int zpool_vdev_fault(zpool_handle_t pool, long _2);
int zpool_vdev_degrade(zpool_handle_t pool, long _2);
int zpool_vdev_clear(zpool_handle_t pool, long _2);

nvlist_t zpool_find_vdev(zpool_handle_t pool, String _2, BooleanByReference _3, BooleanByReference _4);
int zpool_label_disk(libzfs_handle_t lib, zpool_handle_t pool, String label);

/*
 * Functions to manage pool properties
 */
int zpool_set_prop(zpool_handle_t pool, String name, String value);
int zpool_get_prop(zpool_handle_t pool, /* zpool_prop_t */ NativeLong prop, /*char[] */ Pointer buf,
    NativeLong proplen, EnumByReference<zprop_source_t> _5);
long zpool_get_prop_int(zpool_handle_t pool, zpool_prop_t prop, EnumByReference<zprop_source_t> _3);

String zpool_prop_to_name(zpool_prop_t prop);
String zpool_prop_values(zpool_prop_t prop);

/*
 * Pool health statistics.
 */
enum zpool_status_t {
	/*
	 * The following correspond to faults as defined in the (fault.fs.zfs.*)
	 * event namespace.  Each is associated with a corresponding message ID.
	 */
	ZPOOL_STATUS_CORRUPT_CACHE,	/* corrupt /kernel/drv/zpool.cache */
	ZPOOL_STATUS_MISSING_DEV_R,	/* missing device with replicas */
	ZPOOL_STATUS_MISSING_DEV_NR,	/* missing device with no replicas */
	ZPOOL_STATUS_CORRUPT_LABEL_R,	/* bad device label with replicas */
	ZPOOL_STATUS_CORRUPT_LABEL_NR,	/* bad device label with no replicas */
	ZPOOL_STATUS_BAD_GUID_SUM,	/* sum of device guids didn't match */
	ZPOOL_STATUS_CORRUPT_POOL,	/* pool metadata is corrupted */
	ZPOOL_STATUS_CORRUPT_DATA,	/* data errors in user (meta)data */
	ZPOOL_STATUS_FAILING_DEV,	/* device experiencing errors */
	ZPOOL_STATUS_VERSION_NEWER,	/* newer on-disk version */
	ZPOOL_STATUS_HOSTID_MISMATCH,	/* last accessed by another system */
	ZPOOL_STATUS_IO_FAILURE_WAIT,	/* failed I/O, failmode 'wait' */
	ZPOOL_STATUS_IO_FAILURE_CONTINUE, /* failed I/O, failmode 'continue' */
	ZPOOL_STATUS_FAULTED_DEV_R,	/* faulted device with replicas */
	ZPOOL_STATUS_FAULTED_DEV_NR,	/* faulted device with no replicas */

	/*
	 * The following are not faults per se, but still an error possibly
	 * requiring administrative attention.  There is no corresponding
	 * message ID.
	 */
	ZPOOL_STATUS_VERSION_OLDER,	/* older on-disk version */
	ZPOOL_STATUS_RESILVERING,	/* device being resilvered */
	ZPOOL_STATUS_OFFLINE_DEV,	/* device online */

	/*
	 * Finally, the following indicates a healthy pool.
	 */
	ZPOOL_STATUS_OK
}

zpool_status_t zpool_get_status(zpool_handle_t handle, /*char ** */ PointerByReference ppchBuf);
zpool_status_t zpool_import_status(nvlist_t _1, PointerByReference ppchBuf);

/*
 * Statistics and configuration functions.
 */
nvlist_t zpool_get_config(zpool_handle_t pool, /*nvlist_t ** */ PointerByReference ppchNVList);
int zpool_refresh_stats(zpool_handle_t pool, BooleanByReference r);
int zpool_get_errlog(zpool_handle_t pool, /*nvlist_t ** */ PointerByReference ppchNVList);

/*
 * Import and export functions
 */
int zpool_export(zpool_handle_t pool);
int zpool_import(libzfs_handle_t lib, nvlist_t _1, String _2, /*char * */  String altroot);
int zpool_import_props(libzfs_handle_t lib, nvlist_t _1, String _2, nvlist_t _3);

/*
 * Search for pools to import
 */
nvlist_t zpool_find_import(libzfs_handle_t lib, int _1, /*char ** */PointerByReference _2, boolean _3);
nvlist_t zpool_find_import_cached(libzfs_handle_t lib, String _1, boolean _2);

/*
 * Miscellaneous pool functions
 */
//struct zfs_cmd;

String zpool_vdev_name(libzfs_handle_t lib, zpool_handle_t pool, nvlist_t _3);
int zpool_upgrade(zpool_handle_t pool , long _1);
int zpool_get_history(zpool_handle_t pool, /*nvlist_t ** */ PointerByReference ppNVList);
void zpool_set_history_str(String subcommand, int argc, String[] argv, String history_str);
int zpool_stage_history(libzfs_handle_t lib, String _2);
void zpool_obj_to_path(zpool_handle_t pool, long _2, long _3, String _4, NativeLong len);
int zfs_ioctl(libzfs_handle_t lib, int _2, zfs_cmd cmd);
/*
 * Basic handle manipulations.  These functions do not create or destroy the
 * underlying datasets, only the references to them.
 */
zfs_handle_t zfs_open(libzfs_handle_t lib, String name, int/*zfs_type_t*/ typeMask);
void zfs_close(zfs_handle_t handle);
zfs_type_t zfs_get_type(zfs_handle_t handle);
String zfs_get_name(zfs_handle_t handle);

/*
 * Property management functions.  Some functions are shared with the kernel,
 * and are found in sys/fs/zfs.h.
 */

/*
 * zfs dataset property management
 */
String zfs_prop_default_string(zfs_prop_t prop);
long zfs_prop_default_numeric(zfs_prop_t prop);
String zfs_prop_column_name(zfs_prop_t prop);
boolean zfs_prop_align_right(zfs_prop_t prop);

String zfs_prop_to_name(zfs_prop_t prop);

    /**
     * Sets a property on a ZFS data set.
     *
     * <p>
     * This method can set both native properties and user-defined properties.
     */
    int zfs_prop_set(zfs_handle_t handle, String propertyName, String propertyValue);
int zfs_prop_get(zfs_handle_t handle, /* zfs_prop_t */ NativeLong prop, Pointer _4, int cbSize,
    /*zprop_source_t* */ IntByReference _5, char[] _6, NativeLong _7, boolean _8);
int zfs_prop_get_numeric(zfs_handle_t handle, zfs_prop_t prop, LongByReference r,
    /*zprop_source_t* */ IntByReference _4, char[] _5, NativeLong _6);
long zfs_prop_get_int(zfs_handle_t handle, zfs_prop_t prop);
int zfs_prop_inherit(zfs_handle_t handle, String _2);
String zfs_prop_values(zfs_prop_t prop);
int zfs_prop_is_string(zfs_prop_t prop);
nvlist_t zfs_get_user_props(zfs_handle_t handle);

int zfs_expand_proplist(zfs_handle_t handle, /*zprop_list_t ** */ PointerByReference _2);

public static final String ZFS_MOUNTPOINT_NONE= "none";
public static final String ZFS_MOUNTPOINT_LEGACY= "legacy";

/*
 * zpool property management
 */
int zpool_expand_proplist(zpool_handle_t pool, /*zprop_list_t ** */ PointerByReference _2);
String zpool_prop_default_string(zpool_prop_t prop);
long zpool_prop_default_numeric(zpool_prop_t prop);
String zpool_prop_column_name(zpool_prop_t prop);
boolean zpool_prop_align_right(zpool_prop_t prop);

/*
 * Functions shared by zfs and zpool property management.
 */
int zprop_iter(zprop_func func, Pointer arg, boolean show_all, boolean ordered, zfs_type_t type);
int zprop_get_list(libzfs_handle_t lib, String buf, /*zprop_list_t ** */ PointerByReference result, int/*zfs_type_t*/ type);
void zprop_free_list(zprop_list_t arg);

    interface zprop_func extends Callback {
        int callback(int i, Pointer arg);
    }

/*
 * Functions for printing zfs or zpool properties
 */
void zprop_print_one_property(String _1, zprop_get_cbdata_t _2, String _3, String _4, zprop_source_t _5, String _6);

public static final int GET_COL_NAME		=1;
public static final int GET_COL_PROPERTY	=2;
public static final int GET_COL_VALUE		=3;
public static final int GET_COL_SOURCE		=4;

    interface zfs_iter_f extends Callback {
        int callback(zfs_handle_t handle, Pointer arg);
    }

/*
 * Iterator functions.
 */
int zfs_iter_root(libzfs_handle_t lib, zfs_iter_f callback, Pointer arg);
int zfs_iter_children(zfs_handle_t handle, zfs_iter_f callback, Pointer arg);
int zfs_iter_dependents(zfs_handle_t handle, boolean _2, zfs_iter_f callback, Pointer arg);
int zfs_iter_filesystems(zfs_handle_t handle, zfs_iter_f callback, Pointer arg);
int zfs_iter_snapshots(zfs_handle_t handle, zfs_iter_f callback, Pointer arg);

/*
 * Functions to create and destroy datasets.
 */
int zfs_create(libzfs_handle_t lib, String name, int/*zfs_type_t*/ type, nvlist_t props);
int zfs_create_ancestors(libzfs_handle_t lib, String _2);
int zfs_destroy(zfs_handle_t handle);
int zfs_destroy_snaps(zfs_handle_t handle, String name);
int zfs_clone(zfs_handle_t handle, String name, nvlist_t _3);
int zfs_snapshot(libzfs_handle_t lib, String fullNameWithAtSnapShot, boolean recursive);
int zfs_rollback(zfs_handle_t handle1, zfs_handle_t handle2, boolean _3);
int zfs_rename(zfs_handle_t handle, String name, boolean _3);
int zfs_send(zfs_handle_t handle, String _2, String _3, boolean _4, boolean _5, boolean _6, boolean _7, int _8);
int zfs_promote(zfs_handle_t handle);

int zfs_receive(libzfs_handle_t lib, String name, recvflags_t _3, int _4, avl_tree_t _5);

/*
 * Miscellaneous functions.
 */
String zfs_type_to_name(zfs_type_t type);
void zfs_refresh_properties(zfs_handle_t handle);
int zfs_name_valid(String name, zfs_type_t type);
zfs_handle_t zfs_path_to_zhandle(libzfs_handle_t lib, String path, /*zfs_type_t*/ int type);
boolean zfs_dataset_exists(libzfs_handle_t lib, String name, /*zfs_type_t*/int type);
int zfs_spa_version(zfs_handle_t handle, IntByReference r);

/*
 * dataset permission functions.
 */
int zfs_perm_set(zfs_handle_t handle, nvlist_t _2);
int zfs_perm_remove(zfs_handle_t handle, nvlist_t _2);
int zfs_build_perms(zfs_handle_t handle, /*char* */String _2, /*char * */String _3,
    zfs_deleg_who_type_t _4, zfs_deleg_inherit_t _5, /*nvlist_t ** */ PointerByReference _6);
int zfs_perm_get(zfs_handle_t handle, /*zfs_allow_t ***/ PointerByReference _2);
void zfs_free_allows(zfs_allow_t p);
void zfs_deleg_permissions();

/*
 * Mount support functions.
 */
boolean is_mounted(libzfs_handle_t lib, String special, /*char ***/PointerByReference _2);
boolean zfs_is_mounted(zfs_handle_t handle, /*char ***/PointerByReference _3);
int zfs_mount(zfs_handle_t handle, String _2, int mountFlags);

    /**
     *
     * @param umountFlags
     *      Bit combinations from {@link UmountFlags}
     */
    int zfs_unmount(zfs_handle_t handle, String _2, int umountFlags);
    int zfs_unmountall(zfs_handle_t handle, int umountFlags);

/*
 * Share support functions.
 */
boolean zfs_is_shared(zfs_handle_t handle);
int zfs_share(zfs_handle_t handle);
int zfs_unshare(zfs_handle_t handle);

/*
 * Protocol-specific share support functions.
 */
boolean zfs_is_shared_nfs(zfs_handle_t handle, /*char ***/PointerByReference ppch);
boolean zfs_is_shared_smb(zfs_handle_t handle, /*char ***/PointerByReference ppch);
int zfs_share_nfs(zfs_handle_t handle);
int zfs_share_smb(zfs_handle_t handle);
int zfs_shareall(zfs_handle_t handle);
int zfs_unshare_nfs(zfs_handle_t handle, String _2);
int zfs_unshare_smb(zfs_handle_t handle, String _2);
int zfs_unshareall_nfs(zfs_handle_t handle);
int zfs_unshareall_smb(zfs_handle_t handle);
int zfs_unshareall_bypath(zfs_handle_t handle, String _2);
int zfs_unshareall(zfs_handle_t handle);
boolean zfs_is_shared_iscsi(zfs_handle_t handle);
int zfs_share_iscsi(zfs_handle_t handle);
int zfs_unshare_iscsi(zfs_handle_t handle);
    // TODO
//int zfs_iscsi_perm_check(libzfs_handle_t lib, char *, ucred_t *);
//int zfs_deleg_share_nfs(libzfs_handle_t lib, char *, char *,
//    void *, void *, int, zfs_share_op_t);

/*
 * Utility function to convert a number to a human-readable form.
 */
void zfs_nicenum(long _1, /*char **/ char[] buf, NativeLong size);
int zfs_nicestrtonum(libzfs_handle_t lib, String _2, LongByReference r);

/*
 * Given a device or file, determine if it is part of a pool.
 */
int zpool_in_use(libzfs_handle_t lib, int _2, /*pool_state_t* */ IntByReference r, /*char ***/PointerByReference ppch,
    BooleanByReference _5);

/*
 * ftyp special.  Read the label from a given device.
 */
int zpool_read_label(int _1, /*nvlist_t ***/ PointerByReference ppnvlist);

/*
 * Create and remove zvol /dev links.
 */
int zpool_create_zvol_links(zpool_handle_t pool);
int zpool_remove_zvol_links(zpool_handle_t pool);

/* is this zvol valid for use as a dump device? */
int zvol_check_dump_config(/*char **/String _1);

/*
 * Enable and disable datasets within a pool by mounting/unmounting and
 * sharing/unsharing them.
 */
int zpool_enable_datasets(zpool_handle_t pool, String _2, int _3);
int zpool_disable_datasets(zpool_handle_t pool, boolean _2);
}
