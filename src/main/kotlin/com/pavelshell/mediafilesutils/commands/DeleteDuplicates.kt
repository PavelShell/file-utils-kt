package com.pavelshell.mediafilesutils.commands

import com.pavelshell.mediafilesutils.common.FileTreeWalker
import org.springframework.shell.command.annotation.Command
import org.springframework.shell.command.annotation.Option
import java.io.File
import org.bytedeco.opencv.global.opencv_core
import org.bytedeco.opencv.global.opencv_imgcodecs
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.opencv_core.Mat


@Command
class DeleteDuplicates {


    @Command(command = ["delete-duplicates"], description = "Gives a unique names")
    fun run(
        @Option(label = "path", longNames = ["path"], required = true) pathString: String
    ): String {
        val files = FileTreeWalker.getFiles(pathString)
        val checkedFiles = mutableSetOf<File>()
        val duplicates = mutableMapOf<File, Collection<File>>()
        for (fileToCompare in files) {
            if (checkedFiles.contains(fileToCompare)) continue
            checkedFiles += fileToCompare
            for (file in files - checkedFiles) {
                if (areSame(fileToCompare, file)) {
                    checkedFiles += file
                    duplicates.merge(fileToCompare, listOf(file)) { old, new -> old + new }
                }
            }
        }
        return "Found duplicates $duplicates"
    }

    private fun areSame(a: File, b: File): Boolean {
        val img1Matrix = readMatrix(a)
        val img2Matrix = Mat().also { opencv_imgproc.resize(readMatrix(b), it, img1Matrix.size()) }
        val diffMatrix = Mat().also { opencv_core.absdiff(img1Matrix, img2Matrix, it) }
        val diffPixelCount = opencv_core.countNonZero(diffMatrix)
        val similarityCoefficient = 1.0 - (diffPixelCount.toDouble() / (diffMatrix.rows() * diffMatrix.cols()))
        return similarityCoefficient > 0.9
    }

    private fun readMatrix(a: File) = opencv_imgcodecs.imread(a.path, opencv_imgcodecs.IMREAD_GRAYSCALE)
        .also { if (it.empty()) throw IllegalArgumentException("Can't read image $it.") }
}
//delete-duplicates T:\\heap\\pic\\duplicates_test