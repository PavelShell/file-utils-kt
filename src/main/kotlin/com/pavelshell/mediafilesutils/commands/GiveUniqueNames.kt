package com.pavelshell.mediafilesutils.commands

import org.springframework.shell.command.annotation.Command
import org.springframework.shell.command.annotation.Option
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Path
import java.util.*
import kotlin.io.path.exists

@Command
class GiveUniqueNames {

    @Command(command = ["give-unique-names"], description = "Gives a unique names")
    fun run(
        @Option(label = "path", longNames = ["path"], required = true) pathString: String
    ): String {
        val fileOrFolder = Path.of(pathString).let {
            if (!it.exists()) throw FileNotFoundException("File or folder at path $pathString not exists!")
            it.toFile()
        }
        val uuid = UUID.randomUUID()
        var fileNumber = 0
        fileOrFolder.walkTopDown().forEach { file ->
            if (file.isFile) {
                val prefix = "${fileNumber++}$uuid"
                val suffix = if (file.extension.isNotEmpty()) ".${file.extension}" else ""
                val newName = File(file.parentFile, "$prefix$suffix")
                file.renameTo(newName)
            }
        }
        val fileOfFiles = if (fileNumber > 1) "files" else "file"
        return "Done. $fileNumber $fileOfFiles renamed."
    }
}