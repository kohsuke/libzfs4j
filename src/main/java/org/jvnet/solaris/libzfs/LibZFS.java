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

import static org.jvnet.solaris.libzfs.jna.libzfs.LIBZFS;
import static org.jvnet.solaris.nvlist.jna.libnvpair.NV_UNIQUE_NAME;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.jna.Function;
import org.jvnet.solaris.libzfs.jna.libzfs;
import org.jvnet.solaris.libzfs.jna.libzfs.zpool_iter_f;
import org.jvnet.solaris.libzfs.jna.libzfs_handle_t;
import org.jvnet.solaris.libzfs.jna.zfs_handle_t;
import org.jvnet.solaris.libzfs.jna.zfs_type_t;
import org.jvnet.solaris.libzfs.jna.zpool_handle_t;
import org.jvnet.solaris.nvlist.jna.nvlist_t;

import com.sun.jna.Pointer;

/**
 * Entry point to ZFS functionality in Java.
 * 
 * @author Kohsuke Kawaguchi
 * @author Jim Klimov
 */
public class LibZFS implements ZFSContainer {

    private libzfs_handle_t handle;
    private boolean libzfs_enabled = false;
    private String libzfsNotEnabledReason = "";

    /*
     * Track features available in current host ZFS ABI so we can use specific JNA
     * signatures. These will be populated as string tuples (key=value) as defined
     * in this library privately; beside auto-guesswork they can be populated from
     * environment variables (via appserver/init-script setup for hosting Jenkins)
     * as a fallback - but being private, admins should expect to reconfigure such
     * strings when this library gets updated... in reality, auto-guesswork should
     * (be implemented at all and then) do a good job.
     * Note: in this context, the "legacy" ABI refers to function signatures that
     * were in the OpenSolaris codebase prior to split between Oracle and illumos
     * and subsequently OpenZFS. The "openzfs" refers to the state of OpenZFS code
     * "Git HEAD" at the time of relevant edit of these libzfs.jar sources. Keep
     * in mind that downstream projects may lag accepting such changes, maybe for
     * years. For this reason, we keep a toggle for every function where we care
     * about signature differences, and in the (future) case that there are more
     * than these two levels for us to care about (e.g. HEADs in illumos-gate, ZoL
     * and *BSD downstreams of OpenZFS) some interim version strings may be defined
     * and if nothing is defined or value is unknown, then assume "legacy" mode.
     */
    private final Map<String,String> features = new HashMap<String, String>();

    /*package*/ String getFeature(String key) {
        return features.get(key);
    }

    private void initFeatures() {
        String v;
        String n;
        String abi; /* Cache the default while we make decisions */

        n = "LIBZFS4J_ABI";
        v = getSetting(n,"");
        if (v.equals("off") || v.equals("no") || v.equals("disabled") || v.equals("false") || v.equals("NO-OP")) {
            libzfsNotEnabledReason = "libzfs4j not enabled due to user-provided setting: LIBZFS4J_ABI='" + v + "'";
            features.put(n,"NO-OP");
            return;
        }

        if (v.equals("legacy") || v.equals("openzfs")) {
            /* Currently we recognize two values; later it may be more like openzfs-YYYY */
            abi = v;
        } else {
            /* Detect presence of e.g. feature flags routines == openzfs */
            abi = detectCurrentABI();
        }
        features.put(n,abi);

        n = "LIBZFS4J_ABI_zfs_iter_snapshots";
        v = getSetting(n,abi);
        features.put(n,v);

        n = "LIBZFS4J_ABI_zfs_destroy";
        v = getSetting(n,abi);
        features.put(n,v);

        n = "LIBZFS4J_ABI_zfs_destroy_snaps";
        v = getSetting(n,abi);
        features.put(n,v);

        /* Here the expected tweak is "pre-nv96" for VERY old ABI */
        n = "LIBZFS4J_ABI_zfs_snapshot";
        v = getSetting(n,abi);
        features.put(n,v);

        /**
         * This couple of functions was last seen in Sol10u6 and is gone
         * since Sol10u8. More detailed comments in ZFSObject.java::allow()
         * At this time we wrap the old routines and log an error if absent
         * when called; later might find and wrap newer implementations.
         */
        n = "LIBZFS4J_ABI_zfs_perm_set";
        try {
            Function.getFunction("zfs","zfs_perm_set");
            v = getSetting(n,"pre-sol10u8");
        } catch (Throwable e) {
            LOGGER.log(Level.FINEST, "While looking for zfs_perm_set() got this: " + e.toString());
            v = getSetting(n,null);
        }
        features.put(n,v);

        n = "LIBZFS4J_ABI_zfs_perm_remove";
        try {
            Function.getFunction("zfs","zfs_perm_remove");
            v = getSetting(n,"pre-sol10u8");
        } catch (Throwable e) {
            LOGGER.log(Level.FINEST, "While looking for zfs_perm_remove() got this: " + e.toString());
            v = getSetting(n,null);
        }
        features.put(n,v);

        LOGGER.log(Level.FINE, "libzfs4j features: "+features);
    }

    /**
     * Retrieves a feature setting from system property, then from env var.
     */
    private String getSetting(String key, String defaultValue) {
        String v = System.getProperty(key);
        if (v!=null)    return v;

        v = System.getenv(key);
        if (v!=null)    return v;

        /*
         * Avoid using `null' as the defaultValue, so that subsequent
         * calls to stringvar.equals(...) are kept simple.
         */
        if (defaultValue == null)  return "NO-OP";

        return defaultValue;
    }

    /**
     * Makes some effort to find the current ABI (openzfs vs legacy)
     *
     * <p>
     * libzfs doesn't define any obvious version function to help us here, so we use a presence
     * of a method to determine if it's OpenZFS.
     * GitHubID:jimklimov suggested to me in FOSDEM 2017 that feature_is_supported is one such function.
     * Then I discovered that on ZFS on Linux this function is replaced by spa_feature_is_enabled:
     * https://github.com/zfsonlinux/zfs/commit/fa86b5dbb6d33371df344efb2adb0aba026d097c#diff-4b1411a9b1911486460e7ea126a7d9c5
     *
     * ... so here I'm testing both.
     *
     * See https://people.freebsd.org/~gibbs/zfs_doxygenation/html/d4/dd6/zfeature_8h.html
     */
    private String detectCurrentABI() {
        /* This list was retrieved by running
         *   nm /usr/lib/libzfs.so | grep feature | awk '{print "\""$NF"\","}' | sort
         * on different systems */
        String featureFuncs[] = { "deps_contains_feature", "feature_is_supported",
            "spa_feature_is_enabled", "spa_feature_table",
            "zfeature_checks_disable", "zfeature_depends_on", "zfeature_is_supported",
            "zfeature_is_valid_guid", "zfeature_lookup_name", "zfeature_register",
            "zpool_feature_init", "zpool_get_features",
            "zpool_prop_feature", "zpool_prop_get_feature"
            };

        for (String featureFunc : featureFuncs) {
            try {
                LOGGER.log(Level.FINER, "libzfs4j autodetect: looking for " + featureFunc + "()");
                Function.getFunction("zfs",featureFunc);
                return "openzfs";
            } catch (Throwable e) {
                // fall through
                LOGGER.log(Level.FINEST, "While looking for " + featureFunc + "() got this: " + e.toString());
            }
        }
        LOGGER.log(Level.FINER, "libzfs4j autodetect: OpenZFS feature flag support not detected - assuming legacy ZFS");

        return "legacy";
    }

    /**
     * Note that this constructor can throw exceptions if there are errors
     * while initializing the native library (e.g. absent on the host OS).
     * Similarly, it will throw if the end-user configuration explicitly
     * requested to disable this wrapper and not use ZFS features in the
     * calling program. Due to this, callers should not pre-initialize
     * their `new LibZFS()` instances in class member declarations, but
     * rather in constructors or setup methods, and check for exceptions.
     * Or expect such exceptions in callers of classes that might use ZFS.
     */
    public LibZFS() {
        libzfs_enabled = false;
        libzfsNotEnabledReason = "";

        handle = LIBZFS.libzfs_init();
        if (handle==null) {
            libzfsNotEnabledReason = "Failed to initialize libzfs";
        } else {
            initFeatures();
        }

        if (!libzfsNotEnabledReason.isEmpty()) {
            LOGGER.log(Level.FINE, "libzfs4j autodetect: " + libzfsNotEnabledReason);
            throw new LinkageError(libzfsNotEnabledReason);
        }

        libzfs_enabled = true;
    }

    /**
     * Used in routines below to report if this LibZFS instance is not
     * enabled and allow a clean abortion of the corresponding call
     */
    public boolean is_libzfs_enabled(String funcname) {
        if (!libzfs_enabled) {
            LOGGER.log(Level.INFO, "libzfs4j not enabled because: " + libzfsNotEnabledReason + ". Skipped " + funcname + "()");
        }
        return libzfs_enabled;
    }

    /**
     * List up all the root file systems and return them.
     *
     * <p>
     * In ZFS, each zpool gets a top-level zfs file system automatically.
     * This method returns those top-level file systems.
     *
     * @return can be empty but never null.
     */
    public List<ZFSFileSystem> roots() {
        final List<ZFSFileSystem> r = new ArrayList<ZFSFileSystem>();
        if (!is_libzfs_enabled("roots"))
            return r;

        LIBZFS.zfs_iter_root(handle, new libzfs.zfs_iter_f() {
            public int callback(zfs_handle_t handle, Pointer arg) {
                r.add(new ZFSFileSystem(LibZFS.this, handle));
                return 0;
            }
        }, null);
        return r;
    }

    /**
     * Lists up all the ZFS pools.
     *
     * @return can be empty but never null.
     */
    public List<ZFSPool> pools() {
        final List<ZFSPool> r = new ArrayList<ZFSPool>();
        if (!is_libzfs_enabled("pools"))
            return r;

        LIBZFS.zpool_iter(handle, new zpool_iter_f() {
            public int callback(zpool_handle_t handle, Pointer arg) {
                r.add(new ZFSPool(LibZFS.this, handle));
                return 0;
            }
        }, null);
        return r;
    }

    /**
     * Gets the pool of the given name.
     */
    public ZFSPool getPool(String name) {
        zpool_handle_t h = LIBZFS.zpool_open(handle, name);
        if (!is_libzfs_enabled("getPool"))
            return null;

        if(h==null) return null;    // not found
        return new ZFSPool(this,h);
    }

    /**
     * Does a zfs dataset of the given name exist?
     * 
     * @param dataSetName
     *            the dataset name of check for.
     * @return does the dataset exist?
     */
    public boolean exists(final String dataSetName) {
        if (!is_libzfs_enabled("exists"))
            return false;

        final boolean exists = exists(dataSetName, EnumSet.allOf(ZFSType.class));
        return exists;
    }

    /**
     * Does a zfs dataset of the given name and the given types exist?
     * 
     * @param name
     *            the dataset name of check for.
     * @param typeMask
     *            the specific zfs types to check for.
     * @return does the dataset exist?
     */
    public boolean exists(final String name, final Set<ZFSType> typeMask) {
        if (!is_libzfs_enabled("exists"))
            return false;

        int mask = 0;
        for (ZFSType t : typeMask) {
            mask |= t.code;
        }

        final boolean exists = LIBZFS.zfs_dataset_exists(handle, name, mask);
        return exists;
    }

    /**
     * Does a zfs dataset of the given name and the given type exist?
     * 
     * @param dataSetName
     *            the dataset name of check for.
     * @param type
     *            the specific zfs type to check for.
     * @return does the dataset exist?
     */
    public boolean exists(final String dataSetName, final ZFSType type) {
        if (!is_libzfs_enabled("exists"))
            return false;

        final boolean exists = exists(dataSetName, EnumSet.of(type));
        return exists;
    }

    /**
     * Create a ZFS Data Set of a given name and zfs type.
     *
     * @param dataSetName
     *            name of the dataset to create.
     * @param type
     *            the zfs type of dataset to create.
     * @return
     *      Never null. Created dataset.
     */
    public <T extends ZFSObject> T create(String dataSetName, Class<T> type) {
        if (!is_libzfs_enabled("create"))
            return null;

        return type.cast(create(dataSetName, ZFSType.fromType(type), null));
    }

    /**
     * Create a ZFS Data Set of a given name, zfs type and properties.
     * 
     * @param dataSetName
     *            Full name of the dataset to create, like "rpool/abc/def".
     * @param type
     *            the zfs type of dataset to create. Either {@link ZFSType#FILESYSTEM} or {@link ZFSType#VOLUME}.
     * @param props
     *            zfs dataset properties. Can be null.
     * @return created dataset.
     */
    public ZFSObject create(final String dataSetName, final ZFSType type,
            final Map<String, String> props) {
        if (!is_libzfs_enabled("create"))
            return null;

        final nvlist_t nvl = nvlist_t.alloc(NV_UNIQUE_NAME);
        if(props!=null) {
            for (Map.Entry<String, String> e : props.entrySet()) {
                nvl.put(e.getKey(), e.getValue());
            }
        }

        /* create intermediate directories */
        final String[] dirs = dataSetName.split("/");
        final StringBuilder sb = new StringBuilder(dirs[0]);
        for (int i = 1; i < dirs.length; i++) {
            sb.append('/').append(dirs[i]);
            if (!exists(sb.toString())) {
                if (LIBZFS.zfs_create(handle, sb.toString(), type.code, nvl) != 0) {
                    throw new ZFSException(this,"Failed to create "+dataSetName);
                }
            }
        }

        final ZFSObject dataSet = open(dataSetName);
        return dataSet;
    }

    /**
     * Open a ZFS Data Set of a given name.
     * 
     * @param dataSetName
     *            name of the dataset to open.
     * @return opened dataset, or null if no such dataset exists.
     */
    public ZFSObject open(final String dataSetName) {
        if (!is_libzfs_enabled("open"))
            return null;

        final ZFSObject dataSet = open(dataSetName, zfs_type_t.DATASET);
        return dataSet;
    }

    /**
     * Open a ZFS Data Set of a given name and type.
     * 
     * @param dataSetName
     *            name of the dataset to open.
     * @param mask
     *            the zfs type mask of dataset to open.
     * @return opened dataset, or null if no such dataset exists.
     */
    public ZFSObject open(final String dataSetName, final int /* zfs_type_t */mask) {
        if (!is_libzfs_enabled("open"))
            return null;

        zfs_handle_t h = LIBZFS.zfs_open(handle, dataSetName, mask);
        if(h==null) {
            int err = LIBZFS.libzfs_errno(handle);
            if(err==0)  return null;
            throw new ZFSException(this);
        }
        return ZFSObject.create(this,h);
    }

    /**
     * Opens a ZFS dataset of the given name and type.
     */
    public <T extends ZFSObject> T open(String dataSetName, Class<T> type) {
        if (!is_libzfs_enabled("open"))
            return null;

        return type.cast(open(dataSetName,ZFSType.fromType(type).code));
    }

    /**
     * Gets a {@link ZFSFileSystem} mounted at the given directory.
     *
     * @return
     *      null if no such file system exists.
     */
    public ZFSFileSystem getFileSystemByMountPoint(File dir) {
        if (!is_libzfs_enabled("getFileSystemByMountPoint"))
            return null;

        dir = dir.getAbsoluteFile();
        for (ZFSFileSystem f : descendants(ZFSFileSystem.class)) {
            File mp = f.getMountPoint();
            if(mp!=null && mp.equals(dir))
                return f;
        }
        return null;
    }

    public List<ZFSFileSystem> children() {
        if (!is_libzfs_enabled("children"))
            return null;

        return roots();
    }

    @SuppressWarnings("unchecked")
    public <T extends ZFSObject> List<T> children(Class<T> type) {
        if (!is_libzfs_enabled("children"))
            return null;

        if(type.isAssignableFrom(ZFSFileSystem.class))
            return (List)roots();
        else
            return Collections.emptyList();
    }

    public List<ZFSObject> descendants() {
        if (!is_libzfs_enabled("descendants"))
            return null;

        return children(ZFSObject.class);
    }

    public <T extends ZFSObject> List<T> descendants(Class<T> type) {
        if (!is_libzfs_enabled("descendants"))
            return null;

        ArrayList<T> r = new ArrayList<T>();
        r.addAll(children(type));
        for (ZFSFileSystem p : roots())
            r.addAll(p.descendants(type));
        return r;
    }

    /**
     * Returns {@link libzfs_handle_t} that this object wraps.
     * <p>
     * If the caller wants to use methods that don't yet have a high-level
     * binding, the returned {@link libzfs_handle_t} can be used directly in
     * conjunction with {@link libzfs#LIBZFS}.
     */
    public libzfs_handle_t getHandle() {
        return handle;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        dispose();
    }

    /**
     * Eagerly releases the native resource associated with this wrapper,
     * instead of waiting for GC to take care of it.
     */
    public synchronized void dispose() {
        if (handle != null) {
            LIBZFS.libzfs_fini(handle);
            handle = null;
            libzfs_enabled = false;
            libzfsNotEnabledReason = "";
        }
    }

    private static final Logger LOGGER = Logger.getLogger(LibZFS.class.getName());
}
