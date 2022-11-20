package it.sephiroth.gradle.git.utils

object ProcessUtils {

    fun String.exec(): Process {
        println("Executing `${this.trim()}`")
        return Runtime.getRuntime().exec(this.trim())
    }

    fun Process.waitForExitCode(): Int {
        this.waitFor()
        return this.exitValue()
    }

    fun Process.assertExitCode(): Process {
        this.waitForExitCode().let { code ->
            println("exit code: $code")
            check(code == 0) { "Invalid exit code received ($code)" }
        }
        return this
    }

    fun Process.readLines(): List<String> {
        return this.inputStream.reader().readLines()
    }
}
