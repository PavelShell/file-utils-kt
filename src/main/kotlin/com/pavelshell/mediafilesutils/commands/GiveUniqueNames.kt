package com.pavelshell.mediafilesutils.commands

import com.pavelshell.mediafilesutils.common.FileTreeWalker
import org.springframework.shell.command.annotation.Command
import org.springframework.shell.command.annotation.Option
import java.io.File
import java.util.*

@Command
class GiveUniqueNames {

    @Command(command = ["give-unique-names"], description = "Gives a unique names")
    fun run(
        @Option(label = "path", longNames = ["path"], required = true) pathString: String
    ): String {
        val uuid = UUID.randomUUID()
        var fileNumber = 0
        FileTreeWalker.forEachFile(pathString) { file ->
            val prefix = "${fileNumber++}$uuid"
            val suffix = if (file.extension.isNotEmpty()) ".${file.extension}" else ""
            val newName = File(file.parentFile, "$prefix$suffix")
            file.renameTo(newName)
        }
        val fileOfFiles = if (fileNumber > 1) "files" else "file"
        return "Done. $fileNumber $fileOfFiles renamed."
    }
}