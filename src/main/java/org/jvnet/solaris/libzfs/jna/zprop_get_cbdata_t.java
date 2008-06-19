package org.jvnet.solaris.libzfs.jna;

import org.jvnet.solaris.libzfs.zfs_type_t;

/**
 * @author Kohsuke Kawaguchi
 */
public class zprop_get_cbdata_t {
    int cb_sources;
    int[] cb_columns = new int[4];
    int[] cb_colwidths = new int[5];
    boolean cb_scripted;
    boolean cb_literal;
    boolean cb_first;
    zprop_list_t cb_proplist;
    zfs_type_t cb_type;
}
