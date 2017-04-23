package ekoatwork.todomvp.data.source.remote

import android.os.Handler
import ekoatwork.todomvp.data.Task
import ekoatwork.todomvp.data.id
import ekoatwork.todomvp.data.source.CompletionOrFailure
import ekoatwork.todomvp.data.source.DataLoadCallback
import ekoatwork.todomvp.data.source.TaskDataSource
import ekoatwork.todomvp.support.withEach

class RemoteTaskDataSource : TaskDataSource by Instance {

    private object Instance : TaskDataSource {

        private val serviceLatencyMillis: Long = 5000
        private val data: MutableMap<String, Task> = mutableMapOf()

        init {
            saveTask(Task(title = "Build tower of Pisa", description = "Ground looks good."))
            saveTask(Task(title = "Finish bridge in Tacoma.", description = "Found awesome girders at half price."))
        }

        override fun getTasks(callback: DataLoadCallback<Task>) {
            callback.apply {
                delay {
                    onDataLoaded(data.values.toList())
                }
            }
        }

        override fun getTask(taskId: String, callback: (Task?) -> Unit) {
            delay {
                callback(data[taskId])
            }
        }

        override fun saveTask(task: Task, completionOrFailure: ((CompletionOrFailure<Task>) -> Unit)?) {
            data[task.id()] = task
            completionOrFailure?.let { callback -> callback(CompletionOrFailure.Completed(task)) }
        }

        override fun saveTasks(tasks: List<Task>, completionOrFailure: ((List<CompletionOrFailure<Task>>) -> Unit)?) {
            delay {
                tasks.withEach {
                    data[id()] = this
                }
            }
        }


        override fun refreshTasks(refreshed: (() -> Unit)?) {
            // Handled elsewhere!
        }

        override fun deleteTasks(completed: (() -> Unit)?) {
            delay {
                data.clear()
                completed?.invoke()
            }

        }

        override fun deleteTask(taskId: String, deleted: ((taskId: String, removedTask: Task?) -> Unit)?) {
            delay {
                data.remove(taskId).let { removed ->
                    if (deleted!=null) {
                        deleted(taskId, removed)
                    }
                }
            }
        }

        private fun delay(action: () -> Unit) {
            Handler().apply {
                postDelayed(action, serviceLatencyMillis)
            }
        }

    }

}

