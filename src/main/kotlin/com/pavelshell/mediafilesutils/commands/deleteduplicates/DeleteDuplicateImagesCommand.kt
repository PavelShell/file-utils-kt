package com.pavelshell.mediafilesutils.commands.deleteduplicates

import com.pavelshell.mediafilesutils.commands.deleteduplicates.DuplicatesFinder.ComparisonMode
import com.pavelshell.mediafilesutils.common.FileTreeWalker
import org.springframework.shell.command.annotation.Command
import org.springframework.shell.command.annotation.Option
import java.io.File

@Command
class DeleteDuplicateImagesCommand(private val duplicatesFinders: Collection<DuplicatesFinder>) {

    @Command(
        command = ["delete-duplicate-images"],
        description = "For each image in the directory and all sub-directories deletes duplicated images" +
                " and leaves only the one copy with the best quality, judging by size."
    )
    fun run(
        @Option(
            label = "mode",
            longNames = ["mode"],
            required = false,
            defaultValue = "EXACT",
            description = "If SIMILAR mode is used then all similar images with the worse quality will be deleted."
        ) mode: ComparisonMode,
        @Option(
            label = "path",
            longNames = ["path"],
            required = true
        ) pathString: String,
    ): String {
        val filesToCheck = FileTreeWalker.getFiles(pathString)
        val duplicates = findDuplicates(filesToCheck, mode)
        val deletedDuplicatesNumber = deleteDuplicates(duplicates)
        return "Checked ${filesToCheck.size} files and deleted $deletedDuplicatesNumber duplicates."
    }

    private fun findDuplicates(filesToCheck: Collection<File>, mode: ComparisonMode): Set<Set<File>> {
        println("Searching for duplicates...")
        val duplicatesFinder = duplicatesFinders.find { it.comparisonMode == mode }
            ?: throw IllegalArgumentException("$mode mode is not supported!")
        return duplicatesFinder.findDuplicates(filesToCheck)
    }

    private fun deleteDuplicates(allDuplicates: Set<Set<File>>): Int {
        if (allDuplicates.isEmpty()) return 0
        println("Deleting duplicates for ${allDuplicates.size} files...")
        var deletedNumber = 0
        allDuplicates.forEach { duplicates ->
            duplicates
                .toMutableList()
                .sortedBy { it.length() }
                .run { dropLast(1) }
                .forEach {
                    it.delete()
                    deletedNumber++
                }
        }
        return deletedNumber
    }
}
