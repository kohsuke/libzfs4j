package org.jvnet.solaris.libzfs.jna;

enum zprop_source_t {
    // TODO: is this bit mask?
        ZPROP_SRC_NONE, // = 0x1,
	ZPROP_SRC_DEFAULT, // = 0x2,
	ZPROP_SRC_TEMPORARY, // = 0x4,
	ZPROP_SRC_LOCAL, // = 0x8,
	ZPROP_SRC_INHERITED // = 0x10
}
