package it.sephiroth.gradle.git.executor

import it.sephiroth.gradle.git.exception.GitExecutionException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadPoolExecutor

class GitRunner(private val process: Process, val id: Int = 0) : AutoCloseable,
    Comparable<GitRunner> {

    override fun compareTo(other: GitRunner): Int {
        return id.compareTo(other.id)
    }

    fun await(): GitRunner {
        process.waitFor()
        return this
    }

    @Throws(GitExecutionException::class)
    fun assertExitCode(code: Int) {
        if (code != 0) {
            val msg = StringBuilder("Git command failed with exit code $code.\n")
            msg.append(tryReadAllProcessOutputs())
            throw GitExecutionException(msg.toString())
        }
    }

    private fun tryReadAllProcessOutputs(): String? {
        val output = StringBuilder()
        try {
            readText(StdOutput.Output)?.let { str ->
                output.append("Output: ")
                output.append(str)
                output.append("\n")
            }
        } catch (_: Throwable) {
        }
        try {
            readText(StdOutput.Error)?.let { str ->
                output.append("Error: ")
                output.append(str)
            }
        } catch (_: Throwable) {
        }
        return output.toString().takeIf { it.isNotBlank() }
    }

    @Throws(GitExecutionException::class)
    fun assertNoErrors(): GitRunner {
        val exitCode = process.exitValue()
        assertExitCode(exitCode)
        return this
    }

    @Throws(IOException::class)
    fun readLines(
        type: StdOutput = StdOutput.Output,
        charset: Charset = Charsets.UTF_8
    ): List<String> {
        val stream = when (type) {
            StdOutput.Output -> process.inputStream
            StdOutput.Error -> process.errorStream
        }
        return stream.use { s -> readLines(s, charset) }
    }

    @Throws(IOException::class)
    fun readLines(stream: InputStream, charset: Charset = Charsets.UTF_8) =
        stream.reader(charset).readLines().map { it.trim() }

    @Throws(IOException::class)
    fun readText(type: StdOutput = StdOutput.Output, charset: Charset = Charsets.UTF_8): String? {
        val stream = when (type) {
            StdOutput.Output -> process.inputStream
            StdOutput.Error -> process.errorStream
        }
        return stream.use { s -> readText(s, charset) }
    }

    @Throws(IOException::class)
    fun readText(stream: InputStream, charset: Charset = Charsets.UTF_8) =
        stream.reader(charset).readText().takeIf { it.isNotBlank() }

    override fun close() {
        try {
            process.destroy()
        } catch (ignored: Throwable) {
            // ignore
        }
    }

    companion object {
        private val logger: Logger = Logging.getLogger(GitRunner::class.java)
        private val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

        @Throws(IOException::class)
        fun execute(cmd: String): GitRunner {
            logger.quiet("Executing `${cmd.trim()}`...")
            return GitRunner(Runtime.getRuntime().exec(cmd.trim()))
        }

        fun execute(vararg processes: ProcessBuilder): Iterable<Future<GitRunner>> {
            return executor.invokeAll(processes.mapIndexed { index, builder ->
                Callable {
                    logger.quiet("Executing `${builder.command().joinToString(" ")}`...")
                    GitRunner(builder.start(), index).await().assertNoErrors()
                }
            })
        }

        @Throws(IOException::class)
        fun create(command: String): ProcessBuilder {
            return if (command.isEmpty()) {
                throw IllegalArgumentException("Empty command")
            } else {
                val st = StringTokenizer(command)
                val cmdArray = arrayOfNulls<String>(st.countTokens())
                var i = 0
                while (st.hasMoreTokens()) {
                    cmdArray[i] = st.nextToken()
                    ++i
                }
                ProcessBuilder(*cmdArray)
            }
        }
    }

    enum class StdOutput {
        Output, Error
    }
}
