# About libzfs.jar

The [`libzfs.jar`](https://github.com/kohsuke/libzfs4j) is a Java wrapper
for native ZFS functionality as implemented by a host operating system.

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
logging level.

Note that there is more work possible in this area, such as in particular
expanding Jenkins ZFS support to operating systems that do not identify as
a `SunOS`, but this improvement is out of the scope for this update (the
decision is made outside `libzfs.jar` codebase). It could help asking the
wrapper whether it can represent ZFS on the host OS, rather than guessing
by some strings the OS provides, though.

# Kudos

* Kohsuke Kawaguchi
* Jim Klimov
* Oleg Nenashev
* Adam Stevko
