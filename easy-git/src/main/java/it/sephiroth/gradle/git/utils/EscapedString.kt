package it.sephiroth.gradle.git.utils

import it.sephiroth.gradle.git.lib.GitCommand.GitParam

data class EscapedString(val string: String?) : GitParam {

    override fun asQueryString(): String? {
        return string?.let { "\"$string\"" }
    }
}
