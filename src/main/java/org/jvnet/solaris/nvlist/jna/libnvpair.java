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

package org.jvnet.solaris.nvlist.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import org.jvnet.solaris.jna.PtrByReference;

/**
 * @author Kohsuke Kawaguchi
 */
public interface libnvpair extends Library {
    public static final libnvpair LIBNVPAIR = (libnvpair) Native.loadLibrary("nvpair",libnvpair.class);

    enum data_type_t {
            DATA_TYPE_UNKNOWN,
            DATA_TYPE_BOOLEAN,
            DATA_TYPE_BYTE,
            DATA_TYPE_INT16,
            DATA_TYPE_UINT16,
            DATA_TYPE_INT32,
            DATA_TYPE_UINT32,
            DATA_TYPE_INT64,
            DATA_TYPE_UINT64,
            DATA_TYPE_STRING,
            DATA_TYPE_BYTE_ARRAY,
            DATA_TYPE_INT16_ARRAY,
            DATA_TYPE_UINT16_ARRAY,
            DATA_TYPE_INT32_ARRAY,
            DATA_TYPE_UINT32_ARRAY,
            DATA_TYPE_INT64_ARRAY,
            DATA_TYPE_UINT64_ARRAY,
            DATA_TYPE_STRING_ARRAY,
            DATA_TYPE_HRTIME,
            DATA_TYPE_NVLIST,
            DATA_TYPE_NVLIST_ARRAY,
            DATA_TYPE_BOOLEAN_VALUE,
            DATA_TYPE_INT8,
            DATA_TYPE_UINT8,
            DATA_TYPE_BOOLEAN_ARRAY,
            DATA_TYPE_INT8_ARRAY,
            DATA_TYPE_UINT8_ARRAY
    }

//    typedef struct libnvpair {
//            int32_t nvp_size;	/* size of this libnvpair */
//            int16_t	nvp_name_sz;	/* length of name string */
//            int16_t	nvp_reserve;	/* not used */
//            int32_t	nvp_value_elem;	/* number of elements for array types */
//            data_type_t nvp_type;	/* type of value */
//            /* name string */
//            /* aligned ptr array for string arrays */
//            /* aligned array of data for value */
//    } nvpair_t;
//
///* NV allocator framework */
//    typedef struct nv_alloc_ops nv_alloc_ops_t;
//
//    typedef struct nv_alloc {
//            const nv_alloc_ops_t *nva_ops;
//            void *nva_arg;
//    } nv_alloc_t;
//
//    struct nv_alloc_ops {
//            int (*nv_ao_init)(nv_alloc_t *, __va_list);
//            void (*nv_ao_fini)(nv_alloc_t *);
//            void *(*nv_ao_alloc)(nv_alloc_t *, Structure.FFIType.size_t);
//            void (*nv_ao_free)(nv_alloc_t *, void *, Structure.FFIType.size_t);
//            void (*nv_ao_reset)(nv_alloc_t *);
//    };

    /** nvpair names are unique. */
    public static final int NV_UNIQUE_NAME	 =0x1;
    /** Name-data type combination is unique */
    public static final int NV_UNIQUE_NAME_TYPE	 =0x2;


//    int nv_alloc_init(nv_alloc_t *, const nv_alloc_ops_t *, /* args */ ...);
//    void nv_alloc_reset(nv_alloc_t *);
//    void nv_alloc_fini(nv_alloc_t *);

/* list management */
/**
 * The nvlist_alloc() function allocates a new name-value pair list and
 * updates nvlp to point to the handle. The argument nvflag specifies nvlist_t
 * properties to remain persistent across packing, unpacking, and duplication.
 *
 * If NV_UNIQUE_NAME is specified for nvflag, existing nvpairs with matching names are removed
 * before the new nvpair is added. If NV_UNIQUE_NAME_TYPE is specified for nvflag, existing
 * nvpairs with matching names and data types are removed before the new nvpair is added.
 * See nvlist_add_byte(9F) for more details.
 *
 * @param nvflag
 *      Specify bit fields defining nvlist_t properties: NV_UNIQUE_NAME, NV_UNIQUE_NAME_TYPE
 * @param kmflag
 *      Kernel memory allocation policy, either KM_SLEEP or KM_NOSLEEP.
 */
int nvlist_alloc(PtrByReference<nvlist_t> result, int nvflag, int kmflag);
    void nvlist_free(nvlist_t list);
//    int nvlist_size(nvlist_t list, Structure.FFIType.size_t *, int);
//    int nvlist_pack(nvlist_t list, char **, Structure.FFIType.size_t *, int, int);
//    int nvlist_unpack(char *, Structure.FFIType.size_t, nvlist_t list*, int);
//    int nvlist_dup(nvlist_t list, nvlist_t list*, int);
//    int nvlist_merge(nvlist_t list, nvlist_t list, int);
//
//    int nvlist_xalloc(nvlist_t list*, uint_t, nv_alloc_t *);
//    int nvlist_xpack(nvlist_t list, char **, Structure.FFIType.size_t *, int, nv_alloc_t *);
//    int nvlist_xunpack(char *, Structure.FFIType.size_t, nvlist_t list*, nv_alloc_t *);
//    int nvlist_xdup(nvlist_t list, nvlist_t list*, nv_alloc_t *);
//    nv_alloc_t *nvlist_lookup_nv_alloc(nvlist_t list);
//
//    int nvlist_add_nvpair(nvlist_t list, nvpair_t *);
    int nvlist_add_boolean(nvlist_t list, String name);
    int nvlist_add_boolean_value(nvlist_t list, String name, boolean value);
//    int nvlist_add_byte(nvlist_t list, String name, uchar_t);
//    int nvlist_add_int8(nvlist_t list, String name, int8_t);
//    int nvlist_add_uint8(nvlist_t list, String name, uint8_t);
//    int nvlist_add_int16(nvlist_t list, String name, int16_t);
//    int nvlist_add_uint16(nvlist_t list, String name, uint16_t);
//    int nvlist_add_int32(nvlist_t list, String name, int32_t);
//    int nvlist_add_uint32(nvlist_t list, String name, uint32_t);
//    int nvlist_add_int64(nvlist_t list, String name, int64_t);
//    int nvlist_add_uint64(nvlist_t list, String name, uint64_t);
    int nvlist_add_string(nvlist_t list, String key, String value);
    int nvlist_add_nvlist(nvlist_t list, String key, nvlist_t value);
//    int nvlist_add_boolean_array(nvlist_t list, String name, boolean_t *, uint_t);
//    int nvlist_add_byte_array(nvlist_t list, String name, uchar_t *, uint_t);
//    int nvlist_add_int8_array(nvlist_t list, String name, int8_t *, uint_t);
//    int nvlist_add_uint8_array(nvlist_t list, String name, uint8_t *, uint_t);
//    int nvlist_add_int16_array(nvlist_t list, String name, int16_t *, uint_t);
//    int nvlist_add_uint16_array(nvlist_t list, String name, uint16_t *, uint_t);
//    int nvlist_add_int32_array(nvlist_t list, String name, int32_t *, uint_t);
//    int nvlist_add_uint32_array(nvlist_t list, String name, uint32_t *, uint_t);
//    int nvlist_add_int64_array(nvlist_t list, String name, int64_t *, uint_t);
//    int nvlist_add_uint64_array(nvlist_t list, String name, uint64_t *, uint_t);
//    int nvlist_add_string_array(nvlist_t list, String name, char *const *, uint_t);
//    int nvlist_add_nvlist_array(nvlist_t list, String name, nvlist_t list*, uint_t);
//    int nvlist_add_hrtime(nvlist_t list, String name, hrtime_t);
//
//    int nvlist_remove(nvlist_t list, String name, data_type_t);
//    int nvlist_remove_all(nvlist_t list, const char *);
//
//    int nvlist_lookup_boolean(nvlist_t list, const char *);
//    int nvlist_lookup_boolean_value(nvlist_t list, String name, boolean_t *);
//    int nvlist_lookup_byte(nvlist_t list, String name, uchar_t *);
//    int nvlist_lookup_int8(nvlist_t list, String name, int8_t *);
//    int nvlist_lookup_uint8(nvlist_t list, String name, uint8_t *);
//    int nvlist_lookup_int16(nvlist_t list, String name, int16_t *);
//    int nvlist_lookup_uint16(nvlist_t list, String name, uint16_t *);
//    int nvlist_lookup_int32(nvlist_t list, String name, int32_t *);
//    int nvlist_lookup_uint32(nvlist_t list, String name, uint32_t *);
//    int nvlist_lookup_int64(nvlist_t list, String name, int64_t *);
//    int nvlist_lookup_uint64(nvlist_t list, String name, uint64_t *);
    int nvlist_lookup_string(nvlist_t list, String name, PointerByReference result);
    int nvlist_lookup_nvlist(nvlist_t list, String name, PtrByReference<nvlist_t> result);
//    int nvlist_lookup_boolean_array(nvlist_t list, String name,
//        boolean_t **, uint_t *);
//    int nvlist_lookup_byte_array(nvlist_t list, String name, uchar_t **, uint_t *);
//    int nvlist_lookup_int8_array(nvlist_t list, String name, int8_t **, uint_t *);
//    int nvlist_lookup_uint8_array(nvlist_t list, String name, uint8_t **, uint_t *);
//    int nvlist_lookup_int16_array(nvlist_t list, String name, int16_t **, uint_t *);
//    int nvlist_lookup_uint16_array(nvlist_t list, String name, uint16_t **, uint_t *);
//    int nvlist_lookup_int32_array(nvlist_t list, String name, int32_t **, uint_t *);
//    int nvlist_lookup_uint32_array(nvlist_t list, String name, uint32_t **, uint_t *);
//    int nvlist_lookup_int64_array(nvlist_t list, String name, int64_t **, uint_t *);
//    int nvlist_lookup_uint64_array(nvlist_t list, String name, uint64_t **, uint_t *);
//    int nvlist_lookup_string_array(nvlist_t list, String name, char ***, uint_t *);
//    int nvlist_lookup_nvlist_array(nvlist_t list, String name,
//        nvlist_t list**, uint_t *);
//    int nvlist_lookup_hrtime(nvlist_t list, String name, hrtime_t *);
//    int nvlist_lookup_pairs(nvlist_t listnvl, int, ...);
//
//    int nvlist_lookup_nvpair(nvlist_t listnvl, String name, nvpair_t **);
//    boolean_t nvlist_exists(nvlist_t listnvl, const char *);
//
///* processing libnvpair */
//    nvpair_t *nvlist_next_nvpair(nvlist_t listnvl, nvpair_t *);
//    char *nvpair_name(nvpair_t *);
//    data_type_t nvpair_type(nvpair_t *);
//    int nvpair_value_boolean_value(nvpair_t *, boolean_t *);
//    int nvpair_value_byte(nvpair_t *, uchar_t *);
//    int nvpair_value_int8(nvpair_t *, int8_t *);
//    int nvpair_value_uint8(nvpair_t *, uint8_t *);
//    int nvpair_value_int16(nvpair_t *, int16_t *);
//    int nvpair_value_uint16(nvpair_t *, uint16_t *);
//    int nvpair_value_int32(nvpair_t *, int32_t *);
//    int nvpair_value_uint32(nvpair_t *, uint32_t *);
//    int nvpair_value_int64(nvpair_t *, int64_t *);
//    int nvpair_value_uint64(nvpair_t *, uint64_t *);
//    int nvpair_value_string(nvpair_t *, char **);
//    int nvpair_value_nvlist(nvpair_t *, nvlist_t list*);
//    int nvpair_value_boolean_array(nvpair_t *, boolean_t **, uint_t *);
//    int nvpair_value_byte_array(nvpair_t *, uchar_t **, uint_t *);
//    int nvpair_value_int8_array(nvpair_t *, int8_t **, uint_t *);
//    int nvpair_value_uint8_array(nvpair_t *, uint8_t **, uint_t *);
//    int nvpair_value_int16_array(nvpair_t *, int16_t **, uint_t *);
//    int nvpair_value_uint16_array(nvpair_t *, uint16_t **, uint_t *);
//    int nvpair_value_int32_array(nvpair_t *, int32_t **, uint_t *);
//    int nvpair_value_uint32_array(nvpair_t *, uint32_t **, uint_t *);
//    int nvpair_value_int64_array(nvpair_t *, int64_t **, uint_t *);
//    int nvpair_value_uint64_array(nvpair_t *, uint64_t **, uint_t *);
//    int nvpair_value_string_array(nvpair_t *, char ***, uint_t *);
//    int nvpair_value_nvlist_array(nvpair_t *, nvlist_t list**, uint_t *);
//    int nvpair_value_hrtime(nvpair_t *, hrtime_t *);
}
