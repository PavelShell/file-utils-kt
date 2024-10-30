package com.pavelshell.mediafilesutils.commands

import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.shell.test.ShellAssertions
import org.springframework.shell.test.ShellTestClient
import org.springframework.shell.test.autoconfigure.ShellTest
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.pathString

@ShellTest(
    includeFilters = [
        ComponentScan.Filter(type = FilterType.REGEX, pattern = ["com.pavelshell.mediafilesutils.commands.deleteduplicates.*"])
    ]
)
class DeleteDuplicateImagesIntTest {

    @Autowired
    private lateinit var client: ShellTestClient

    @Test
    fun `should show command in a list of commands`() {
        val session = client
            .nonInterative("help")
            .run()

        await().atMost(3, TimeUnit.SECONDS).untilAsserted {
            ShellAssertions.assertThat(session.screen()).containsText("delete-duplicate-images")
        }
    }

    @Test
    fun `should delete straight duplicates and similar images`() {
        // given
        val testImagesDir = Files.createTempDirectory("some-dir")
            .also { File("src/test/resources/duplicated-images").copyRecursively(it.toFile()) }


        // when
        val session = client
            .nonInterative(
                "delete-duplicate-images",
                "--mode", "SIMILAR",
                "--path", testImagesDir.pathString.replace(BACKSLASH, BACKSLASH.repeat(2))
            )
            .run()

        // then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted {
            ShellAssertions.assertThat(session.screen())
                .containsText("Checked 14 files and deleted 5 duplicates.")
        }
        val imagesWithoutDuplicates = testImagesDir.listFiles().map { it.name }
        Assertions.assertEquals(9, imagesWithoutDuplicates.size)
        Assertions.assertTrue(imagesWithoutDuplicates.contains("BMxrdohJ_gg.jpg"))
        Assertions.assertTrue(imagesWithoutDuplicates.contains("cIpTWpqEu9o.jpg"))
        Assertions.assertTrue(imagesWithoutDuplicates.contains("icon1.png"))
        Assertions.assertTrue(imagesWithoutDuplicates.contains("icon2.png"))
        Assertions.assertTrue(imagesWithoutDuplicates.contains("test-compress-5-percentchanged-big.jpg"))
        Assertions.assertTrue(imagesWithoutDuplicates.contains("test1.jpg"))
        Assertions.assertTrue(imagesWithoutDuplicates.contains("test2.jpg"))
        Assertions.assertTrue(
            imagesWithoutDuplicates.contains("aFdxgzWCuig - Copy.jpg")
                    || imagesWithoutDuplicates.contains("aFdxgzWCuig.jpg")
        )
    }

    private fun Path.listFiles() = toFile().listFiles() ?: arrayOf()

    companion object {
        private const val BACKSLASH = "\\"
    }
}