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
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.*


@ShellTest(
    includeFilters = [
        ComponentScan.Filter(type = FilterType.REGEX, pattern = ["com.pavelshell.mediafilesutils.commands.*"])
    ]
)
class GiveUniqueNamesIntTest {

    @Autowired
    private lateinit var client: ShellTestClient

    @Test
    fun `should show command in a list of commands`() {
        val session = client
            .nonInterative("help")
            .run()

        await().atMost(2, TimeUnit.SECONDS).untilAsserted {
            ShellAssertions.assertThat(session.screen()).containsText("give-unique-names")
        }
    }

    @Test
    fun `should rename single file`() {
        // given
        val directoryPath = Files.createTempDirectory("some-dir")
        val filePath = Files.createFile(directoryPath.resolve("IDDQD")).apply { writeText("some text 123") }
        val fileContents = filePath.readText()

        // when
        val session = client
            .nonInterative(
                "give-unique-names",
                "--path",
                filePath.pathString.replace(BACKSLASH, BACKSLASH.repeat(2))
            )
            .run()

        // then
        await().atMost(2, TimeUnit.SECONDS).untilAsserted {
            ShellAssertions.assertThat(session.screen()).containsText("Done. 1 file renamed.")
        }
        Assertions.assertEquals(1, directoryPath.listFiles().size)
        val newFilePath = directoryPath.listFiles().first().toPath()
        Assertions.assertNotEquals(filePath.name, newFilePath.name)
        Assertions.assertEquals(fileContents, newFilePath.readText())
    }

    @Test
    fun `should rename all files in the directory`() {
        // given
        val dir0 = Files.createTempDirectory("some-dir")
        val dir0File0 = Files.createFile(dir0.resolve("A")).apply { writeText("i'm the first file") }
        val dir0File0Contents = dir0File0.readText()
        val dir0File1 = Files.createFile(dir0.resolve("B")).apply { writeText("i'm the second file") }
        val dir0File1Contents = dir0File1.readText()
        val dir1 = Files.createDirectory(dir0.resolve("folded-dir"))
        val dir1File0 = Files.createFile(dir1.resolve("A")).apply { writeText("i'm the file in a directory") }
        val dir1File0Contents = dir1File0.readText()


        // when
        val session = client
            .nonInterative(
                "give-unique-names",
                "--path",
                dir0.pathString.replace(BACKSLASH, BACKSLASH.repeat(2))
            )
            .run()

        // then
        await().atMost(2, TimeUnit.SECONDS).untilAsserted {
            ShellAssertions.assertThat(session.screen()).containsText("Done. 3 files renamed.")
        }
        Assertions.assertTrue(dir0.exists(), "Should neither delete nor rename the directory with files")
        Assertions.assertEquals(3, dir0.listFiles().size)
        val dir0File0New = dir0.listFiles().first { it.isFile }
        Assertions.assertNotEquals(dir0File0.name, dir0File0New.name)
        Assertions.assertEquals(dir0File0Contents, dir0File0New.readText())
        val dir0File1New = dir0.listFiles().last { it.isFile }
        Assertions.assertNotEquals(dir0File1.name, dir0File1New.name)
        Assertions.assertEquals(dir0File1Contents, dir0File1New.readText())
        Assertions.assertTrue(dir0File0New.name < dir0File1New.name)
        Assertions.assertTrue(dir1.exists())
        Assertions.assertEquals(1, dir1.listFiles().size)
        val dir1File0New = dir1.listFiles().first()
        Assertions.assertNotEquals(dir1File0.name, dir1File0New.name)
        Assertions.assertEquals(dir1File0Contents, dir1File0New.readText())
    }

    private fun Path.listFiles() = toFile().listFiles() ?: arrayOf()

    companion object {
        private const val BACKSLASH = "\\"
    }
}
