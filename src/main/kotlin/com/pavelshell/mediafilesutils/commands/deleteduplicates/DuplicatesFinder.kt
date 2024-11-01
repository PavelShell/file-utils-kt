package com.pavelshell.mediafilesutils.commands.deleteduplicates

import java.io.File

interface DuplicatesFinder {
    /**
     * Type of comparison algorithm used to find duplicates.
     */
    val comparisonMode: ComparisonMode

    /**
     * Finds duplicates among provided [files]
     */
    fun findDuplicates(files: Collection<File>): Set<Set<File>>

    /**
     * Enum of file comparison algorithms.
     */
    enum class ComparisonMode {

        /**
         * Exact comparison by content match.
         */
        EXACT,

        /**
         * Loose comparison based on content "similarity" of files.
         * Allows to find all files with the similar content but different quality.
         */
        SIMILAR
    }
}
