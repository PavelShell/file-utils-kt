package com.pavelshell.mediafilesutils

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.shell.command.annotation.CommandScan

@SpringBootApplication
@CommandScan("com.pavelshell.mediafilesutils.commands")
class MediaFilesUtilsApplication

fun main(args: Array<String>) {
    runApplication<MediaFilesUtilsApplication>(*args)
}