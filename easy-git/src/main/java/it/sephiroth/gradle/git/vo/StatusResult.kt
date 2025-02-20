package it.sephiroth.gradle.git.vo

data class StatusResult internal constructor(
    val localBranc: String? = null,
    val remoteBranch: String? = null,
    val files: List<StatusFile>
) {

    class StatusFile internal constructor(
        val filePath: String,
        val types: List<StatusFileType>
    ) {

        override fun toString(): String {
            return "StatusFile(file='$filePath', status=${types.joinToString(",")})"
        }
    }

    enum class StatusFileType {
        Added, Modified, Deleted, Untracked;

        companion object {
            internal fun of(input: String): List<StatusFileType> {
                return when (input) {
                    "??" -> listOf(Untracked)
                    else -> {
                        input.mapNotNull { char ->
                            when (char) {
                                'A' -> Added
                                'M' -> Modified
                                'D' -> Deleted
                                else -> null
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        private val lineReg = "^([AMD]+|\\?\\?)\\s+([^\\n]+)$".toRegex()
        private val branchReg = "^##\\s+([\\w]+)(...([\\w\\/]+))?(.*)?".toRegex()

        fun parse(lines: List<String>): StatusResult {
            var localBranch: String? = null
            var remoteBranch: String? = null

            val files = mutableListOf<StatusFile>()
            lines.forEachIndexed { index, line ->
                if (index == 0) {
                    branchReg.matchEntire(line)?.let { match ->
                        localBranch = match.groupValues[1]
                        remoteBranch = match.groupValues[3]
                        return@forEachIndexed
                    }
                }

                lineReg.matchEntire(line)?.let { match ->
                    val type = match.groupValues[1]
                    val file = match.groupValues[2]
                    files.add(StatusFile(file.trim(), StatusFileType.of(type)))
                } ?: run {
                    println("Failed to match $line")
                }
            }
            return StatusResult(localBranch, remoteBranch, files)
        }
    }
}
