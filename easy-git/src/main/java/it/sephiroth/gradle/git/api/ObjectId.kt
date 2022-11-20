package it.sephiroth.gradle.git.api

data class ObjectId(val id: String) {

    override fun toString(): String {
        return "ObjectId(id='$id')"
    }

    companion object {
        fun of(value: String?): ObjectId {
            checkNotNull(value)
            return ObjectId(value)
        }
    }
}
