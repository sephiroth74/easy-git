package it.sephiroth.gradle.git.api

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.sephiroth.gradle.git.lib.GitBranchListCommand
import it.sephiroth.gradle.git.lib.GitDeleteLocalBranchCommand
import it.sephiroth.gradle.git.lib.GitRevParseCommand
import it.sephiroth.gradle.git.lib.Repository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GitBranchTest {

    private lateinit var mockGit: Git
    private lateinit var mockRepository: Repository
    private lateinit var mockRevParseCommand: GitRevParseCommand

    @BeforeEach
    fun setUp() {
        mockGit = mockk()
        mockRepository = mockk()
        mockRevParseCommand = mockk()

        every { mockGit.repository } returns mockRepository
    }

    @Test
    fun `should get current branch name`() {
        every { mockRepository.resolve(Repository.HEAD) } returns mockRevParseCommand
        every { mockRevParseCommand.abbrevRef(GitRevParseCommand.AbbrevRefMode.Strict) } returns mockRevParseCommand
        every { mockRevParseCommand.call() } returns listOf("main")

        val gitBranch = GitBranch(mockGit)
        val branchName = gitBranch.name()

        assertEquals("main", branchName)
        verify { mockRepository.resolve(Repository.HEAD) }
        verify { mockRevParseCommand.abbrevRef(GitRevParseCommand.AbbrevRefMode.Strict) }
        verify { mockRevParseCommand.call() }
    }

    @Test
    fun `should return null when no branch name found`() {
        every { mockRepository.resolve(Repository.HEAD) } returns mockRevParseCommand
        every { mockRevParseCommand.abbrevRef(GitRevParseCommand.AbbrevRefMode.Strict) } returns mockRevParseCommand
        every { mockRevParseCommand.call() } returns emptyList()

        val gitBranch = GitBranch(mockGit)
        val branchName = gitBranch.name()

        assertNull(branchName)
    }

    @Test
    fun `should create GitBranchListCommand`() {
        val gitBranch = GitBranch(mockGit)
        val listCommand = gitBranch.list()

        assertNotNull(listCommand)
        assertEquals(GitBranchListCommand::class, listCommand::class)
    }

    @Test
    fun `should create GitDeleteLocalBranchCommand`() {
        val gitBranch = GitBranch(mockGit)
        val deleteCommand = gitBranch.delete("feature-branch")

        assertNotNull(deleteCommand)
        assertEquals(GitDeleteLocalBranchCommand::class, deleteCommand::class)
    }
}