# About libzfs.jar

The [`libzfs.jar`](https://github.com/kohsuke/libzfs4j) is a Java wrapper
for native ZFS functionality as implemented by a host operating system.

If you are here because of stack traces or segmentation fault logs which
mention ZFS in a Java program, please take a look at the [Troubleshooting](README.md#Troubleshooting)
section below.

# A bit of turbulent history

Since the ZFS filesystem, data integrity and volume management subsystem was
introduced in the Sun Solaris 10 operating system over a decade ago (in 2005),
the main interaction API was to use command-line tools. Also an OS-internal
native library `libzfs.so` was available, but its purpose was interfacing
those tools to the OS kernel. In absence of other ABIs, a few projects still
risked to link against this "uncommitted" library -- including the `libzfs.jar`
wrapper employed by [Jenkins](http://jenkins-ci.org/) continuous integration
and automation server, to use on matching platforms for tasks like snapshot
management, etc. and a number of other projects [e.g. on
GitHub](https://github.com/search?l=Maven+POM&q=libzfs&type=Code&utf8=%E2%9C%93)

As time went and Solaris evolved, some function signatures were changed in
the library, causing a moderate bit of headache for consumers like this
wrapper -- but it could be handwaved by requiring an upgrade to the newest
release of the single OS.

As more time had passed, the Sun OpenSolaris project and later the
[illumos](http://illumos.org) project and numerous open-source distributions
based on that had splintered off from Solaris (which, as a brand and product,
went along its own proprietary path and versioning under Oracle stewardship).

Also the ZFS technologies were adopted into multiple other operating system
kernels (including several BSD's, Linux and MacOS efforts), cross-pollinating
and evolving under the roof of the [OpenZFS project](http://open-zfs.org/).
The `libzfs.so` library is still an "uncommitted implementation detail" of
respective distributions, with random ABI changes causing now a much bigger
headache due to fragmentation -- both because there is no single ZFS-wielding
OS that you can require an upgrade to, and because there are dozens of
combinations of possible function signatures that can be present in a
particular instance and update-level of an OS deployment.

The effect for `libzfs.jar` and Jenkins in particular was that as the CI
application server booted up on an OS with incompatible function signatures
(and began interacting with ZFS datasets), its Java process just dumped core
and died -- and with evolution of OpenZFS consuming and contributing operating
systems, it became more likely than not to expect a wrong single fixed ABI.

Recent (2017) changes in this project aimed to specifically improve the
portability of [Jenkins](https://jenkins.io/) to illumos distros -- the
"SunOS" descendants based on modern OpenZFS. Also added was testability
of this wrapper's codebase via Jenkins and Travis CI as integrated on GitHub,
so contributors can test their improvements before posting a pull request.

The accepted solution for improvement of `libzfs.jar` wrapper portability
was to identify which of the wrapped routines have different native ABI
signatures "present in the wild" and introduce a way to pick and call the
correct signature during run-time. While the updated JAR tries its best to
guess the correct set for a given host OS, it also includes a way for the
end-user (sysadmin) to enforce particular implementation for each such
routine as well as the overall default, using environment variables or Java
properties -- and since such settings can be passed from environment outside
the Jenkins web-application, they would survive eventual upgrades of the
`jenkins.war`). Also this technique is expandable, to uniformly handle more
such cases as the future comes down upon us and the `libzfs.jar` sources
have to be updated again :)

For details about currently supported names and values for such toggles
please see the source code for your version of `libzfs.jar` (this is at
the moment regarded as "implementation detail" so options are not listed
here), or refer to the current Git HEAD status:

* Routines with `ABI` comments in
  https://github.com/kohsuke/libzfs4j/blob/master/src/main/java/org/jvnet/solaris/libzfs/jna/libzfs.java
* Manipulation with strings containing `LIBZFS4J` in `api.equals()` calls in
  https://github.com/kohsuke/libzfs4j/blob/master/src/main/java/org/jvnet/solaris/libzfs/LibZFS.java
  and https://github.com/kohsuke/libzfs4j/blob/master/src/main/java/org/jvnet/solaris/libzfs/ZFSObject.java
  (later maybe other sources)

Settings ultimately applied to each toggle can be seen in application server
(or standalone Jetty app) log if you start it with a `FINE` or greater log4j
logging level. See also the `lockpick-libzfs-abi.sh` script that tries out
the currently known toggle options and their values, to pick the correct
settings for an end-user's deployment.

From practice, for late versions of Sun Solaris and several half a decade of
operating systems and ZFS modules based on illumos and OpenZFS codebases,
a likely end-user setup (e.g. in application server settings) would be:

````
LIBZFS4J_ABI=openzfs LIBZFS4J_ABI_zfs_iter_snapshots=legacy
````
while for illumos-based OSes with kernel since mid-2016 it would be all-new:
````
LIBZFS4J_ABI=openzfs
````

Note that there is more work possible in this area, such as in particular
expanding Jenkins ZFS support to operating systems that do not identify as
a `SunOS`, but this improvement is out of the scope for this update (the
decision is made outside `libzfs.jar` codebase). It could help asking the
wrapper whether it can represent ZFS on the host OS, rather than guessing
by some strings the OS provides, though.

At this time one can wrap calls to initialization of a `LibZFS` instance
in caller's set-up method (rather than using a pre-initialized `static
final` class member) and catch resulting exceptions -- this should wrap
both absence of ZFS on the host OS (or other inability to use it) and the
end-user's explicit request to not use the wrapper by `-DLIBZFS4J_ABI=off`.
See `LibZFSTest.java` for more details.

# Troubleshooting

It is likely that you've got to this repository because your Jenkins server
did not start, or even had its JVM segfaulted, with hints pointing at ZFS.

It should be helpful to increase log4j level of your application server to
`FINE` or above, so this library would report more details about its activity
to help you pin-point where and why it failed.

Certain situations are known to cause issues for out-of-the-box setups:

* Mismatch of LibZFS ABI between the native code in your operating system
  libraries and the Java Native Interfaces in this repository. The JVM of
  a Jenkins server would crash a couple of minutes after startup, producing
  some `hs_err_pid*.log` files with stack traces that mention `zfs`.

  * See above about setting up the ABI to use for each routine that is
    known to have evolved into having several binary signatures, using
    application server properties or environment variables. In particular,
    give a shot to the `lockpick-libzfs-abi.sh` script that tries out
    the currently known toggle options and their values, to pick the
    correct settings for an end-user's deployment. OSes and their native
    `libzfs.so` interfaces do evolve and change over time, making older
    "good" settings obsolete.

  * Work with your OS distribution community or vendor to pre-package
    a Jenkins service (or other packages using `libzfs4j`) that would
    take these settings into account.

  * If the problem is with a routine whose signature is not handled by
    this library, please develop, test on your OS and propose a pull
    request at https://github.com/kohsuke/libzfs4j to handle the new
    binary dialect. See existing code for examples of handling this.

* Too many datasets (including snapshots) on the Jenkins server - long
startup and/or crash due to exhausting JVM memory. Might happen during
calls into native code, then producing stack traces way too similar (on
the first glance) to the ABI mismatch issue. Logging at a `FINE` level
would show that your server looks at snapshots of all ZFS datasets in
order, and maybe would log some out-of-heap errors before stalling or
crashing; tools like `top` would show the `java` process size growing
until its configured limit.

  * Constrain the ZFS scope visible to Jenkins by running it in a zone
    or otherwise dedicated environment.

  * Limit the amount of snapshots made on your system, such as by an
    automatic snapshots service (zfs-auto-snap, time-slider, znapzend
    etc.).

  * As a temporary fix, try increasing the `-Xmx` setting of your JVM and
    throw lots of RAM at it.

  * Fix Jenkins-core and/or this library to not track the whole universe by
    default during startup - it should suffice to know just the datasets
    mounted under `JENKINS_HOME` and maybe a few other similar locations.

# Kudos

* Kohsuke Kawaguchi
* Jim Klimov
* Oleg Nenashev
* Adam Stevko
