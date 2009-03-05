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

/**
 * Pool health statistics.
 * @author Kohsuke Kawaguchi
 */
public enum ZPoolStatus {
	/*
	 * The following correspond to faults as defined in the (fault.fs.zfs.*)
	 * event namespace.  Each is associated with a corresponding message ID.
	 */
    /** corrupt /kernel/drv/zpool.cache */
	ZPOOL_STATUS_CORRUPT_CACHE,
	ZPOOL_STATUS_MISSING_DEV_R("One or more devices could not be opened. Sufficient replicas exist for the pool to continue functioning in a degraded state.","Attach the missing device and online it using 'zpool online'."),	/* missing device with replicas */
	ZPOOL_STATUS_MISSING_DEV_NR("One or more devices could not be opened. There are insufficient replicas for the pool to continue functioning.","Attach the missing device and online it using 'zpool online'."),	/* missing device with no replicas */
	ZPOOL_STATUS_CORRUPT_LABEL_R("One or more devices could not be used because the label is missing or invalid. Sufficient replicas exist for the pool to continue functioning in a degraded state.","Replace the device using 'zpool replace'."),	/* bad device label with replicas */
	ZPOOL_STATUS_CORRUPT_LABEL_NR("One or more devices could not be used because the label is missing or invalid. There are insufficient replicas for the pool to continue functioning.","Destroy and re-create the pool from a backup source."),	/* bad device label with no replicas */
	ZPOOL_STATUS_BAD_GUID_SUM,	/* sum of device guids didn't match */
	ZPOOL_STATUS_CORRUPT_POOL("The pool metadata is corrupted and the pool cannot be opened.","Destroy and re-create the pool from a backup source."),	/* pool metadata is corrupted */
	ZPOOL_STATUS_CORRUPT_DATA("One or more devices has experienced an error resulting in data corruption.  Applications may be affected.","Restore the file in question if possible.  Otherwise restore the entire pool from backup."),	/* data errors in user (meta)data */
	ZPOOL_STATUS_FAILING_DEV("One or more devices has experienced an unrecoverable error.  An attempt was made to correct the error.  Applications are unaffected.","Determine if the device needs to be replaced, and clear the errors using 'zpool clear' or replace the device with 'zpool replace'."),	/* device experiencing errors */
	ZPOOL_STATUS_VERSION_NEWER("The pool has been upgraded to a newer, incompatible on-disk version. The pool cannot be accessed on this system.","Access the pool from a system running more recent software, or restore the pool from backup."),	/* newer on-disk version */
	ZPOOL_STATUS_HOSTID_MISMATCH,	/* last accessed by another system */
	ZPOOL_STATUS_IO_FAILURE_WAIT("One or more devices are faulted in response to IO failures.","Make sure the affected devices are connected, then run 'zpool clear'."),	/* failed I/O, failmode 'wait' */
	ZPOOL_STATUS_IO_FAILURE_CONTINUE("One or more devices are faulted in response to IO failures.","Make sure the affected devices are connected, then run 'zpool clear'."), /* failed I/O, failmode 'continue' */
	ZPOOL_STATUS_BAD_LOG("An intent log record could not be read.\tWaiting for adminstrator intervention to fix the faulted pool.","Either restore the affected device(s) and run 'zpool online',\tor ignore the intent log records by running 'zpool clear'."),		/* cannot read log chain(s) */

    ZPOOL_STATUS_FAULTED_DEV_R("One or more devices are faulted in response to persistent errors. Sufficient replicas exist for the pool to continue functioning in a degraded state.","Replace the faulted device, or use 'zpool clear' to mark the device repaired."),	/* faulted device with replicas */
	ZPOOL_STATUS_FAULTED_DEV_NR("One or more devices are faulted in response to persistent errors.  There are insufficient replicas for the pool to continue functioning.","Destroy and re-create the pool from a backup source.  Manually marking the device\trepaired using 'zpool clear' may allow some data to be recovered."),	/* faulted device with no replicas */

	/*
	 * The following are not faults per se, but still an error possibly
	 * requiring administrative attention.  There is no corresponding
	 * message ID.
	 */
	ZPOOL_STATUS_VERSION_OLDER("The pool is formatted using an older on-disk format.  The pool can still be used, but some features are unavailable.","Upgrade the pool using 'zpool upgrade'.  Once this is done, the pool will no longer be accessible on older software versions."),	/* older on-disk version */
	ZPOOL_STATUS_RESILVERING("One or more devices is currently being resilvered.  The pool will continue to function, possibly in a degraded state.","Wait for the resilver to complete."),	/* device being resilvered */
	ZPOOL_STATUS_OFFLINE_DEV("One or more devices has been taken offline by the administrator. Sufficient replicas exist for the pool to continue functioning in a degraded state.","Online the device using 'zpool online' or replace the device with 'zpool replace'."),	/* device online */

	/*
	 * Finally, the following indicates a healthy pool.
	 */
	ZPOOL_STATUS_OK;

    /**
     * Human readable description of what this status means.
     * Can be null.
     */
    public final String status;
    /**
     * Human readable description of how to resolve the problem.
     * Can be null.
     */
    public final String action;

    ZPoolStatus(String status, String action) {
        this.status = status;
        this.action = action;
    }

    ZPoolStatus() {
        this(null,null);
    }
}
