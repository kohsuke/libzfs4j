# About libzfs.jar

This is a Java wrapper for native ZFS functionality as implemented by a host
operating system.

# A bit of turbulent history

Since the ZFS filesystem, data integrity and volume management subsystem was
introduced in the Sun Solaris 10 operating system over a decade ago (in 2005),
the main interaction API was to use command-line tools. Also an OS-internal
native library `libzfs.so` was available, but its purpose was interfacing
those tools to the OS kernel. In absence of other ABIs, a few projects still
risked to link against this "uncommitted" library -- including the `libzfs.jar`
wrapper employed by Jenkins CI on matching platforms for tasks like snapshot
management, etc.

As time went and Solaris evolved, some function signatures were changed in
the library, causing a moderate bit of headache for consumers like this
wrapper -- but it could be handwaved by requiring an upgrade to the newest
release of the single OS.

As more time had passed, the Sun OpenSolaris project and later illumos and
numerous open-source distributions had splintered off from Solaris (which,
as a brand, went along its own proprietary path and versioning under Oracle
stewardship).

Also the ZFS technologies were adopted into multiple other operating system
kernels (including several BSD's, Linux and MacOS efforts), cross-pollinating
under the foundations of OpenZFS project. The `libzfs.so` library is still
an "uncommitted implementation detail" of respective distributions, with
random ABI changes causing now a much bigger headache due to fragmentation --
because there is no single ZFS-wielding OS that you can require an upgrade to,
and because there are dozens of combinations of possible function signatures
that can be present in a particular OS deployment.

The effect for `libzfs.jar` and Jenkins CI in particular was that as the CI
application server booted up on an OS with incompatible function signatures
(and began interacting with ZFS datasets), its Java process just dumped core
and died -- and with evolution of OpenZFS consuming and contributing operating
systems, it became more likely than not to expect a wrong single fixed ABI.

Recent (2017) changes in this project aimed to specifically improve the
portability of Jenkins CI to illumos distros -- the "SunOS" descendants based
on modern OpenZFS, and added testability of this wrapper's codebase via
Jenkins and Travis CI as integrated on Github.

The accepted solution for `libzfs.jar` wrapper was to identify which of the
wrapped routines have different native ABI signatures "in the wild" and
introduce a way to pick and call the correct signature during run-time.
While the updated JAR tries its best to guess the correct set for a given
host OS, it includes a way for the end-user (sysadmin) to enforce particular
implementation for each such function, using environment variables or java
properties -- and since such settings can be passed from environment outside
the Jenkins web-application, they would survive eventual upgrades of the
`jenkins.war`). Also this technique is expandable, to uniformly handle more
such cases as the future comes down upon us and the `libzfs.jar` sources
have to be updated again :)

Note that there is more work possible in this area, such as expanding Jenkins
ZFS support to operating systems that do not identify as a `SunOS`, but such
improvements are out of the scope for this update (and the decision is even
made outside `libzfs.jar` codebase).

# Kudos

* Kohsuke Kawaguchi
* Jim Klimov
* Oleg Nenashev
* Adam Stevko
