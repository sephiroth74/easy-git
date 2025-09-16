package it.sephiroth.gradle.git.executor

import it.sephiroth.gradle.git.exception.GitExecutionException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.util.StringTokenizer
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class GitRunner(
    proc: Process? = null,
    val tag: Any? = null,
) : AutoCloseable {

    constructor(builder: ProcessBuilder, tag: String?) : this(null, tag) {
        this.processBuilder = builder
    }

    private var processBuilder: ProcessBuilder? = null

    private lateinit var process: Process

    init {
        proc?.let {
            check(processBuilder == null)
            process = it
        }
    }

    fun await(): GitRunner {
        processBuilder?.let { builder ->
            logger.quiet(
                "Executing `${
                    builder.command().joinToString(" ")
                }` on ${Thread.currentThread()}"
            )
            process = builder.start()
        }
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
        if (exitCode != 0) {
            logger.warn("Git command failed with exit code $exitCode.")
        }
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

        var numThreads: Int = Runtime.getRuntime().availableProcessors() / 2

        private val executor: ExecutorService by lazy {
            logger.lifecycle("Using $numThreads threads")
            Executors.newFixedThreadPool(numThreads)
        }

        @Throws(IOException::class)
        fun execute(cmd: String, workingDir: File): GitRunner {
            logger.quiet("Executing `${cmd.trim()}`...")
            return create(cmd, null, workingDir)
        }

        @Throws(IOException::class)
        fun execute(commands: List<String>, workingDir: File): GitRunner {
//            logger.quiet("Executing `${commands.joinToString(" ")}`...")
            return create(commands, null, workingDir)
        }

        fun execute(vararg processes: GitRunner): Iterable<Future<GitRunner>> {
            logger.quiet("Executing ${processes.size} processes...")
            return executor.invokeAll(processes.mapIndexed { _, runner ->
                Callable {
                    runner.await().assertNoErrors()
                }
            })
        }

        @Throws(IOException::class)
        fun create(commands: List<String>, tag: String? = null, workingDir: File): GitRunner {
            logger.quiet("Creating `${commands.joinToString(" ")}`...")
            return GitRunner(ProcessBuilder(commands).directory(workingDir), tag)
        }

        @Throws(IOException::class)
        fun create(command: String, tag: String? = null, workingDir: File): GitRunner {
            logger.quiet("Creating `$command`...")
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
                GitRunner(ProcessBuilder(*cmdArray).directory(workingDir), tag)
            }
        }
    }

    enum class StdOutput {
        Output, Error
    }
}
