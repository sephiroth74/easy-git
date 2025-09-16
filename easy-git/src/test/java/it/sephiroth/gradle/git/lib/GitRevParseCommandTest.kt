package it.sephiroth.gradle.git.lib

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import it.sephiroth.gradle.git.api.Git
import it.sephiroth.gradle.git.executor.GitRunner
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GitRevParseCommandTest {

    private lateinit var mockRepository: Repository
    private lateinit var mockRepoDir: File
    private lateinit var mockGitRunner: GitRunner

    @BeforeEach
    fun setUp() {
        mockRepository = mockk()
        mockRepoDir = mockk()
        mockGitRunner = mockk()

        every { mockRepository.repoDir } returns mockRepoDir
        every { mockRepoDir.toString() } returns "/test/repo"

        mockkObject(GitRunner)
        every { GitRunner.execute(any<String>(), any<File>()) } returns mockGitRunner
        every { mockGitRunner.await() } returns mockGitRunner
        every { mockGitRunner.assertNoErrors() } returns mockGitRunner
        every { mockGitRunner.readLines(GitRunner.StdOutput.Output) } returns listOf("main")
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(GitRunner)
    }

    @Test
    fun `should execute rev-parse command with default parameters`() {
        val command = GitRevParseCommand(mockRepository)
        val result = command.call()

        assertEquals(listOf("main"), result)
        verify { GitRunner.execute("git --no-pager rev-parse HEAD", mockRepoDir) }
    }

    @Test
    fun `should execute rev-parse command with short parameter`() {
        val command = GitRevParseCommand(mockRepository)
        command.short(7)
        command.call()

        verify { GitRunner.execute("git --no-pager rev-parse --short=7 HEAD", mockRepoDir) }
    }

    @Test
    fun `should execute rev-parse command with abbrev-ref parameter`() {
        val command = GitRevParseCommand(mockRepository)
        command.abbrevRef(GitRevParseCommand.AbbrevRefMode.Strict)
        command.call()

        verify { GitRunner.execute("git --no-pager rev-parse --abbrev-ref=strict HEAD", mockRepoDir) }
    }

    @Test
    fun `should add abbrev-ref parameter without value when mode is Auto`() {
        val command = GitRevParseCommand(mockRepository)
        command.abbrevRef(GitRevParseCommand.AbbrevRefMode.Auto)
        command.call()

        verify { GitRunner.execute("git --no-pager rev-parse --abbrev-ref HEAD", mockRepoDir) }
    }

    @Test
    fun `should execute rev-parse command with symbolic-full-name parameter`() {
        val command = GitRevParseCommand(mockRepository)
        command.symbolicFullName()
        command.call()

        verify { GitRunner.execute("git --no-pager rev-parse --symbolic-full-name HEAD", mockRepoDir) }
    }

    @Test
    fun `should execute rev-parse command with symbolic parameter`() {
        val command = GitRevParseCommand(mockRepository)
        command.symbolic()
        command.call()

        verify { GitRunner.execute("git --no-pager rev-parse --symbolic HEAD", mockRepoDir) }
    }

    @Test
    fun `should execute rev-parse command with custom commit id`() {
        val command = GitRevParseCommand(mockRepository)
        command.commitId("abc123")
        command.call()

        verify { GitRunner.execute("git --no-pager rev-parse abc123", mockRepoDir) }
    }

    @Test
    fun `should execute rev-parse command with multiple parameters`() {
        val command = GitRevParseCommand(mockRepository)
        command.short(8)
        command.abbrevRef(GitRevParseCommand.AbbrevRefMode.Loose)
        command.commitId("def456")
        command.call()

        verify { GitRunner.execute("git --no-pager rev-parse --short=8 --abbrev-ref=loose def456", mockRepoDir) }
    }

    @Test
    fun `AbbrevRefMode enum should have correct values`() {
        assertEquals("auto", GitRevParseCommand.AbbrevRefMode.Auto.asQueryString())
        assertEquals("loose", GitRevParseCommand.AbbrevRefMode.Loose.asQueryString())
        assertEquals("strict", GitRevParseCommand.AbbrevRefMode.Strict.asQueryString())
    }
}