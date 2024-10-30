package com.pavelshell.mediafilesutils.commands.deleteduplicates

import java.io.File

/**
 * Used to find duplicates among provided files.
 */
abstract class AbstractDuplicatesFinder {

    /**
     * Type of comparison algorithm used to find duplicates.
     */
    abstract val comparisonMode: ComparisonMode

    /**
     * Finds duplicates among provided [files]
     */
    abstract fun findDuplicates(files: Collection<File>): Map<File, Collection<File>>

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