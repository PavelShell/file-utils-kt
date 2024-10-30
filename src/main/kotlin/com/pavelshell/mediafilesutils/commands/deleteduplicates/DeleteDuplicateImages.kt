package com.pavelshell.mediafilesutils.commands.deleteduplicates

import com.pavelshell.mediafilesutils.common.FileTreeWalker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.shell.command.annotation.Command
import org.springframework.shell.command.annotation.Option

@Command
class DeleteDuplicateImages(private val duplicatesFinders: Collection<AbstractDuplicatesFinder>) {
//    delete-duplicate-images --mode SIMILAR --path "T:\\heap\\pic\\duplicates_test - Copy"
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
        ) mode: AbstractDuplicatesFinder.ComparisonMode,
        @Option(
            label = "path",
            longNames = ["path"],
            required = true
        ) pathString: String,
    ): String = runBlocking(Dispatchers.Default) {
        val filesToCheck = FileTreeWalker.getFiles(pathString)
        val duplicatesFinder = duplicatesFinders.find { it.comparisonMode == mode }
            ?: throw IllegalArgumentException("$mode mode is not supported!")
        val duplicates = duplicatesFinder.findDuplicates(filesToCheck)
        println("Deleting duplicates...")
        duplicates.forEach { (file, sameFiles) ->
            (sameFiles + file)
                .toMutableList()
                .sortedBy { it.length() }
                .run { dropLast(1) }
                .forEach { it.delete() }
        }
        return@runBlocking "Checked ${filesToCheck.size} files " +
                "and deleted ${duplicates.values.flatten().distinct().size} duplicates."
    }
}
