package com.ncorti.ktfmt.gradle

public enum class TrailingCommaManagementStrategy {
    /** Do not manage trailing commas at all, only format what is already present */
    NONE,
    /**
     * Only add trailing commas when necessary, but do not remove them.
     *
     * Lists that cannot fit on one line will have trailing commas inserted. Trailing commas can to
     * be used to "hint" ktfmt that the list should be broken to multiple lines.
     */
    ONLY_ADD,
    /**
     * Fully manage trailing commas, adding and removing them where necessary.
     *
     * Lists that cannot fit on one line will have trailing commas inserted. Lists that span
     * multiple lines will have them removed. Manually inserted trailing commas cannot be used as a
     * hint to force breaking lists to multiple lines.
     */
    COMPLETE,
}
