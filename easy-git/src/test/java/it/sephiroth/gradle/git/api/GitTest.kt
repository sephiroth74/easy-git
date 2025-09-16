package it.sephiroth.gradle.git.api

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import it.sephiroth.gradle.git.lib.Repository
import org.gradle.api.Project
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame

class GitTest {

    private lateinit var mockProject: Project
    private lateinit var mockRootProject: Project
    private lateinit var mockRootDir: File

    @BeforeEach
    fun setUp() {
        mockProject = mockk()
        mockRootProject = mockk()
        mockRootDir = mockk()

        every { mockProject.rootProject } returns mockRootProject
        every { mockRootProject.rootDir } returns mockRootDir
        every { mockRootDir.toString() } returns "/test/repo"
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Repository::class)
    }

    @Test
    fun `should create Git instance with valid project`() {
        val git = Git.get(mockProject)

        assertNotNull(git)
        assertNotNull(git.branch)
        assertNotNull(git.tag)
        assertNotNull(git.diff)
        assertNotNull(git.log)
        assertNotNull(git.remote)
    }

    @Test
    fun `should return same instance for same project (singleton)`() {
        val git1 = Git.get(mockProject)
        val git2 = Git.get(mockProject)

        assertSame(git1, git2)
    }

    @Test
    fun `should create Git instance with File`() {
        val testDir = File("/test/repo")
        val git = Git.open(testDir)

        assertNotNull(git)
        assertNotNull(git.repository)
    }

    @Test
    fun `should have correct version constant`() {
        assertEquals("1.0.26", Git.VERSION)
    }

    @Test
    fun `should have build date constant`() {
        assertEquals(1739980980254L, Git.BUILD_DATE)
    }
}