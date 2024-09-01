package com.pavelshell.mediafilesutils.commands

import com.pavelshell.mediafilesutils.common.FileTreeWalker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.bytedeco.opencv.global.opencv_core
import org.bytedeco.opencv.global.opencv_imgcodecs
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.opencv_core.DMatchVector
import org.bytedeco.opencv.opencv_core.KeyPointVector
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Size
import org.bytedeco.opencv.opencv_features2d.BFMatcher
import org.springframework.shell.command.annotation.Command
import org.springframework.shell.command.annotation.Option
import java.io.File

@Command
class DeleteDuplicateImages {

    @Command(
        command = ["delete-duplicate-images"],
        description = "For each image in the directory and all sub-directories deletes duplicated images" +
                " and leaves only the one copy with the best quality, judging by size."
    )
    fun run(
        @Option(label = "path", longNames = ["path"], required = true) pathString: String
    ): String = runBlocking(Dispatchers.Default) {
        val filesToCheck = FileTreeWalker.getFiles(pathString)
            .map { async { it to computeKeyPointsDescriptors(readMatrix(it)) } }
            .awaitAll()
            .toMap()

        val checkedFiles = mutableSetOf<File>()
        val duplicates = mutableMapOf<File, Collection<File>>()
        filesToCheck.entries.forEach { (file, descriptor) ->
            if (!checkedFiles.contains(file)) {
                checkedFiles += file
                val similarFiles = findSimilarByDescriptor((filesToCheck - checkedFiles), descriptor)
                duplicates[file] = similarFiles
                checkedFiles += similarFiles
                println("Checked ${checkedFiles.size} files...")
            }
        }

        println("Deleting duplicates...")
        duplicates.forEach { (file, sameFiles) ->
            (sameFiles + file)
                .toMutableList()
                .sortedBy { it.length() }
                .run { dropLast(1) }
                .forEach { it.delete() }
        }
        return@runBlocking "Checked ${filesToCheck.size} files " +
                "and deleted ${duplicates.values.flatten().size} duplicates."
    }

    private fun findSimilarByDescriptor(files: Map<File, Mat>, originalDescriptor: Mat): List<File> {
        val matches = mutableListOf<File>()
        for ((file, descriptor) in files) {
            val areSame = computeDescriptorsDifference(descriptor, originalDescriptor) < FILE_MATCH_THRESHOLD
            if (areSame) {
                matches += file
            }
        }
        return matches
    }

    private fun Mat.resizeTo(size: Size): Mat = Mat().also { opencv_imgproc.resize(this, it, size) }

    private fun readMatrix(a: File) = opencv_imgcodecs.imread(a.path, opencv_imgcodecs.IMREAD_GRAYSCALE)
        .also { if (it.empty()) throw IllegalArgumentException("Can't read image $a.") }
        .resizeTo(Size(500, 500))

    private fun computeKeyPointsDescriptors(image: Mat): Mat {
        val keyPoints = KeyPointVector().also { ORB.detect(image, it) }
        val descriptors = Mat().also { ORB.compute(image, keyPoints, it) }
        return descriptors
    }

    private fun computeDescriptorsDifference(descriptors1: Mat, descriptors2: Mat): Double {
        val matches = DMatchVector()
        MATCHER.match(descriptors1, descriptors2, matches)
        val totalDistance = matches.get().sumOf { it.distance().toDouble() }
        return totalDistance / matches.get().size
    }

    private companion object {
        private const val FILE_MATCH_THRESHOLD = 15
        private val ORB = org.bytedeco.opencv.opencv_features2d.ORB.create()
        private val MATCHER = BFMatcher(opencv_core.NORM_HAMMING, true)
    }
}
