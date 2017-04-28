package ekoatwork.todomvp.data

import java.util.*

data class Task(var id: String ? = null,
                var title: String? = null,
                var description: String? = null,
                var completed: Boolean = false)


val Task.isActive : Boolean get() = !completed
val Task.isEmpty : Boolean get() = title.isNullOrEmpty() and description.isNullOrEmpty()
val Task.isSaved : Boolean get() = id == null

fun Task.id(): String {
    return synchronized(this) {
        if (id == null) id = "${UUID.randomUUID()}"
        id!!
    }
}