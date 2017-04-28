package ekoatwork.todomvp

import android.content.Context
import ekoatwork.todomvp.data.source.TaskRepository
import ekoatwork.todomvp.data.source.local.TaskLocalDataSource
import ekoatwork.todomvp.data.source.remote.RemoteTaskDataSource

class Injection private constructor(context: Context) {

    val taskRepository: TaskRepository = TaskRepository(RemoteTaskDataSource(), TaskLocalDataSource(context))

    companion object {
        private const val lock = 1
        private var singleton: Injection? = null
        operator fun invoke(context: Context): Injection {
            return synchronized(lock) {
                if (singleton == null) {
                    singleton = Injection(context)
                }
                singleton!!
            }
        }
    }
}