package it.sephiroth.gradle.git.vo

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * @param name author name
 * @param email author email
 * @param isoDate author date strict iso 8601 format
 */
data class Author(val name: String, val email: String?, val isoDate: String?) {

    val dateTime: OffsetDateTime?
        get() {
            return isoDate?.let { d ->
                OffsetDateTime.parse(d, DateTimeFormatter.ISO_DATE_TIME)
            }
        }

    override fun toString(): String {
        return "Author(name='$name', email=$email, dateTime=$isoDate)"
    }
}
