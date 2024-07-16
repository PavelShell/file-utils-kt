package com.pavelshell.mediafilesutils.common

import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Path
import kotlin.io.path.exists

/**
 * Utility class that contains methods used to walk the file tree.
 */
object FileTreeWalker {

    fun getFiles(path: String): List<File> = mutableListOf<File>().also { list -> forEachFile(path) { list.add(it) } }

    fun forEachFile(path: String, action: (File) -> Unit) {
        val fileOrFolder = Path.of(path).let {
            if (!it.exists()) throw FileNotFoundException("File or folder at path $path not exists!")
            it.toFile()
        }
        fileOrFolder.walkTopDown().forEach { if (it.isFile) action(it) }
    }
}