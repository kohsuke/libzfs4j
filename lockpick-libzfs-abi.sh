#! /usr/bin/env bash

# Try to "lockpick" the settings relevant for libzfs.jar on this host OS
# Note that the "correct answer" may change across OS updates... as well
# as libzfs.jar evolution. Requires sources, "mvn" and JDK at this time.
#
# TODO: Add an option to libzfstest to run just a certain native routine.
#
# Copyright (C) 2017 by Jim Klimov

# Bashism to allow pipes to fail not only due to last called program
# Also, below, bash associative arrays are used
set -o pipefail

build_libzfs() {
    echo "Building latest libzfs4j.jar and tests..."
    mvn compile test-compile 2>/dev/null >/dev/null && echo "OK" || \
        { RES=$?; echo "FAILED to build code" >&2; exit $RES; }
}

#LIBZFSTEST_MVN_OPTIONS="${LIBZFSTEST_MVN_OPTIONS-} -Dlibzfs.test.loglevel=FINEST"
#LIBZFSTEST_MVN_OPTIONS="${LIBZFSTEST_MVN_OPTIONS-} -Dlibzfs.test.pool=mydatapool/mydataset"
LIBZFSTEST_MVN_OPTIONS="${LIBZFSTEST_MVN_OPTIONS-} -Dlibzfs.test.pool=rpool"

test_libzfs() (
    echo ""
    echo "Testing with the following settings:"
    SETTINGS="$(set | egrep '^LIBZFS4J_.*=')"
    echo "$SETTINGS"

    RES=0
    MAVEN_OPTS="-XX:ErrorFile=/dev/null"
    export MAVEN_OPTS
    OUT="$(mvn -DargLine=-XX:ErrorFile=/dev/null $LIBZFSTEST_MVN_OPTIONS $* test 2>&1)" || RES=$?
    if [ "$VERBOSE" = yes ]; then
        echo "$OUT"
    else
        echo "$OUT" | egrep '^FINE.*LIBZFS4J' | uniq
    fi

    case "$RES" in
        0|1) # Test could fail e.g. due to inaccessible datasets
            echo "SUCCESS ($RES)"
            return 0
            ;;
        134|*) # 134 = 128 + 6 = coredump on OS signal SEGABRT
            echo "FAILED ($RES) with the settings above:" $SETTINGS >&2
            ;;
    esac
    return $RES
)

test_defaults() {
    echo "Try with default settings and an auto-guesser..."
    for LIBZFS4J_ABI in \
        legacy \
        openzfs \
        "" \
    ; do
        test_libzfs && exit
    done
}


# Put most-probable variants first, to reduce amount of iterations
declare -A LIBZFS_VARIANT_FUNCTIONS
LIBZFS_VARIANT_FUNCTIONS["zfs_iter_snapshots"]=" legacy openzfs"
#LIBZFS_VARIANT_FUNCTIONS["zfs_iter_snapshots"]="openzfs legacy"
LIBZFS_VARIANT_FUNCTIONS["zfs_snapshot"]="legacy openzfs pre-nv96"
#LIBZFS_VARIANT_FUNCTIONS["zfs_snapshot"]="openzfs legacy pre-nv96"
LIBZFS_VARIANT_FUNCTIONS["zfs_destroy_snaps"]="openzfs legacy"
LIBZFS_VARIANT_FUNCTIONS["zfs_destroy"]="openzfs legacy"
# TODO: New ABI syntax for either major branch of ZFS has not yet been
# figured out, so routines are sort of deprecated (NO-OPs) until then.
# Still, to allow yet older Solarises to run well, we test the old ABI first.
LIBZFS_VARIANT_FUNCTIONS["zfs_perm_remove"]="pre-sol10u8 NO-OP"
LIBZFS_VARIANT_FUNCTIONS["zfs_perm_set"]="pre-sol10u8 NO-OP"

#LIBZFS4J_ABI_zfs_destroy_snaps=openzfs, LIBZFS4J_ABI_zfs_iter_snapshots=openzfs, LIBZFS4J_ABI_zfs_destroy=openzfs, LIBZFS4J_ABI_zfs_perm_remove=NO-OP, LIBZFS4J_ABI_zfs_perm_set=NO-OP, LIBZFS4J_ABI_zfs_snapshot=openzfs, LIBZFS4J_ABI=openzfs

##################### DO THE WORK #########################

build_libzfs

# Value set in looped calls below
export LIBZFS4J_ABI
#test_defaults

# Override the default for individual variants explicitly in the loop below
LIBZFS4J_ABI=legacy
echo ""
echo "Simple approach failed - begin lockpicking..."
for ZFS_FUNCNAME in "${!LIBZFS_VARIANT_FUNCTIONS[@]}" ; do
    echo ""
#    echo "ZFS_FUNCNAME='$ZFS_FUNCNAME'"
    for ZFS_VARIANT in ${LIBZFS_VARIANT_FUNCTIONS[${ZFS_FUNCNAME}]} "" ; do
#        echo " ZFS_VARIANT='$ZFS_VARIANT'"
        eval LIBZFS4J_ABI_${ZFS_FUNCNAME}="${ZFS_VARIANT}"
        eval export LIBZFS4J_ABI_${ZFS_FUNCNAME}
        echo "Testing function variant LIBZFS4J_ABI_${ZFS_FUNCNAME}='${ZFS_VARIANT}'..."
        test_libzfs -Dlibzfs.test.funcname="${ZFS_FUNCNAME}" -X && break
    done
done

echo ""
echo "Re-validating the full set of lockpicking results..."
VERBOSE=yes test_libzfs && exit

exit 1
