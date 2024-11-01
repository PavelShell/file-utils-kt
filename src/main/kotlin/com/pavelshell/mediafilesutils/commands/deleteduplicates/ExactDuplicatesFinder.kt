package com.pavelshell.mediafilesutils.commands.deleteduplicates

import com.pavelshell.mediafilesutils.commands.deleteduplicates.DuplicatesFinder.ComparisonMode
import org.apache.commons.io.FileUtils
import org.springframework.stereotype.Component
import java.io.File

/**
 * Finds all files with the exact same content.
 */
@Component
class ExactDuplicatesFinder(
    override val comparisonMode: ComparisonMode = ComparisonMode.EXACT
) : DuplicatesFinder {

    override fun findDuplicates(files: Collection<File>): Set<Set<File>> {
        val allDuplicates = mutableSetOf<Set<File>>()
        files.groupBy { it.length() }.values.forEach { filesWithSameSize ->
            val checkedFiles = mutableSetOf<File>()
            filesWithSameSize.forEach { file ->
                if (!checkedFiles.contains(file)) {
                    checkedFiles += file
                    val duplicates = filesWithSameSize.filter { possibleDuplicate ->
                        !checkedFiles.contains(possibleDuplicate) && FileUtils.contentEquals(possibleDuplicate, file)
                    }.toSet()
                    if (duplicates.isNotEmpty()) {
                        allDuplicates += (duplicates + file)
                    }
                    checkedFiles += duplicates
                }
            }
        }
        return allDuplicates
    }
}