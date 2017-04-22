#! /usr/bin/env bash

# Try to "lockpick" the settings relevant for libzfs.jar on this host OS
# Note that the "correct answer" may change across OS updates... as well
# as libzfs.jar evolution. Requires sources, "mvn" and JDK at this time.
# Note it is likely to leave around Java coredump files, or at least logs
# of those with stack traces.
#
# TODO: Add an option to libzfstest to run just a certain native routine.
#
# Copyright (C) 2017 by Jim Klimov
#
# See also some docs:
#  http://maven.apache.org/surefire/maven-surefire-plugin/examples/single-test.html

# Bashism to allow pipes to fail not only due to last called program
# Also, below, bash associative arrays are used
set -o pipefail

# We do not care for these coredumps
# But the hs_err_pid*.log files are not so easy to avoid
ulimit -c 0

[ -n "${VERBOSITY-}" ] || VERBOSITY=quiet

die() {
    RES="$1"
    [ -n "$RES" ] && [ "$RES" -gt 0 ] && shift || RES=1
    if [ -n "$*" ]; then
        echo "FATAL: $@" >&2
    fi
    exit $RES
}

build_libzfs() {
    echo "Building latest libzfs4j.jar and tests..."
    mvn compile test-compile 2>/dev/null >/dev/null && echo "OK" || \
        die $? "FAILED to build code"
}

### NOTE: Better just create that dataset and `zfs allow` your account to test
### in it. Or use
### "$(mkfile -v 16M testpool.img && zpool create testpool `pwd`/testpool.img)"
### to create a test pool (as root)...
###   sudo zfs allow -ld jim mount,create,share,destroy,snapshot rpool/kohsuke
LIBZFSTEST_DATASET="rpool/kohsuke"
LIBZFSTEST_MVN_OPTIONS="${LIBZFSTEST_MVN_OPTIONS-} -Dlibzfs.test.pool=${LIBZFSTEST_DATASET}"
LIBZFSTEST_MVN_OPTIONS="${LIBZFSTEST_MVN_OPTIONS-} -Dlibzfs.test.loglevel=FINEST"
### Avoid parallel test-cases inside our class - it is unreadable to debug
LIBZFSTEST_MVN_OPTIONS="${LIBZFSTEST_MVN_OPTIONS-} -Dparallel=classes -DforkCount=0"

test_libzfs() (
    echo ""
    echo "Testing with the following settings:"
    SETTINGS="$(set | egrep '^LIBZFS4J_.*=')"
    echo "$SETTINGS"

    RES=0
    DUMPING_OPTS="-XX:ErrorFile=/dev/null -Xmx64M"
    MAVEN_OPTS="${DUMPING_OPTS}"
    export MAVEN_OPTS
    OUT="$(mvn -DargLine="${DUMPING_OPTS}" $LIBZFSTEST_MVN_OPTIONS $* test 2>&1)" || RES=$?
    case "$VERBOSITY" in
    high)
        echo "$OUT" | egrep '^FINE.*LIBZFS4J' | uniq
        echo "$OUT" | egrep -v '^FINE.*LIBZFS4J|org.jvnet.solaris.libzfs.LibZFS initFeatures|^$'
        ;;
    yes)
        echo "$OUT" | egrep '^FINE.*LIBZFS4J' | uniq
        echo "$OUT" | egrep 'testfunc_' | uniq
        ;;
    quiet|*) ;;
    esac

    case "$RES" in
        0|1) # Test could fail e.g. due to inaccessible datasets
            echo "SUCCESS (did not core-dump, returned $RES)"
            if [ "$TEST_OK_ZERO_ONLY" = yes ]; then
                return $RES
            fi
            return 0
            ;;
        134|*) # 134 = 128 + 6 = coredump on OS signal SEGABRT
            echo "FAILED ($RES) with the settings above:" $SETTINGS >&2
            ;;
    esac
    return $RES
)

test_linkability() {
    echo "Test usability of Native ZFS from Java..."
    TEST_OK_ZERO_ONLY=yes test_libzfs -Dlibzfs.test.funcname=testCouldStart -X >/dev/null 2>&1 \
        && echo SUCCESS || die $? "Does this host have libzfs.so?"
}

test_defaults() {
    echo "Try with default settings and an auto-guesser..."
    for LIBZFS4J_ABI in \
        legacy \
        openzfs \
        "" \
    ; do
        test_libzfs && return 0
    done
}

test_all_routines() {
    echo "Re-validate specific routines"
    test_libzfs -Dlibzfs.test.funcname="${!LIBZFS_VARIANT_FUNCTIONS[*]}"
}

# Put most-probable variants first, to reduce amount of iterations
declare -A LIBZFS_VARIANT_FUNCTIONS
LIBZFS_VARIANT_FUNCTIONS["zfs_iter_snapshots"]=" legacy openzfs"
#LIBZFS_VARIANT_FUNCTIONS["zfs_iter_snapshots"]="openzfs legacy"
LIBZFS_VARIANT_FUNCTIONS["zfs_snapshot"]="pre-nv96 legacy openzfs"
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

test_linkability

#test_defaults && test_all_routines && exit

# Override the default for individual variants explicitly in the loop below
LIBZFS4J_ABI=legacy
echo ""
echo "Simple approach failed - begin lockpicking..."
for ZFS_FUNCNAME in "${!LIBZFS_VARIANT_FUNCTIONS[@]}" ; do
    eval LIBZFS4J_ABI_${ZFS_FUNCNAME}="NO-OP"
    eval export LIBZFS4J_ABI_${ZFS_FUNCNAME}
done

for ZFS_FUNCNAME in "${!LIBZFS_VARIANT_FUNCTIONS[@]}" ; do
    echo ""
    # Note: Empty token must be in the end - for library picking defaults,
    # and as the fatal end of loop if nothing tried works for this system.
    for ZFS_VARIANT in ${LIBZFS_VARIANT_FUNCTIONS[${ZFS_FUNCNAME}]} "" ; do
        eval LIBZFS4J_ABI_${ZFS_FUNCNAME}="${ZFS_VARIANT}"
        eval export LIBZFS4J_ABI_${ZFS_FUNCNAME}
        echo "Testing function variant LIBZFS4J_ABI_${ZFS_FUNCNAME}='${ZFS_VARIANT}'..."
        test_libzfs -Dlibzfs.test.funcname="${ZFS_FUNCNAME}" -X && break
        if [ -z "$ZFS_VARIANT" ]; then
            die 1 "FAILED to find a working variant for $ZFS_FUNCNAME"
        fi
        #die 1 "After first test"
    done
    echo "============== Picked function variant LIBZFS4J_ABI_${ZFS_FUNCNAME}='${ZFS_VARIANT}'..."
    #die 1 "After one loop"
done

echo ""
echo "Re-validating the full set of lockpicking results..."
VERBOSITY=high test_libzfs && VERBOSITY=high test_all_routines || die $? "FAILED re-validation"

echo ""
echo "Packaging the results..."
mvn $LIBZFSTEST_MVN_OPTIONS package || die $? "FAILED packaging"

exit 0
