package ekoatwork.todomvp.data.source

import ekoatwork.todomvp.data.Task
import ekoatwork.todomvp.data.id
import ekoatwork.todomvp.data.source.local.TaskLocalDataSource
import ekoatwork.todomvp.data.source.remote.RemoteTaskDataSource
import ekoatwork.todomvp.support.withEach
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class TaskRepository private constructor(

        private val remoteTaskDataSource: RemoteTaskDataSource,
        private val localDataSource: TaskLocalDataSource) : TaskDataSource {

    internal var taskCache: MutableMap<String, Task>? = null
    internal var cacheIsDirty = false

    companion object {

        private val lock = 1
        private var singleInstance: TaskRepository? = null
        operator fun invoke(remoteTaskDataSource: RemoteTaskDataSource, localDataSource: TaskLocalDataSource) {
            synchronized(lock) {
                if (singleInstance == null) singleInstance = TaskRepository(remoteTaskDataSource, localDataSource)
            }
        }

        fun destroy() {
            synchronized(lock) {
                singleInstance = null
            }
        }
    }

    override fun getTasks(callback: DataLoadCallback<Task>) {

        if ((taskCache != null) and (!cacheIsDirty)) {
            callback.onDataLoaded(taskCache!!.values.toList())
            return
        }

        if (cacheIsDirty) {
            getTaskFromRemoteRepository(callback)
        } else {
            localDataSource.getTasks(object : DataLoadCallback<Task> {
                override fun onDataLoaded(data: List<Task>) {
                    refreshCache(data)
                    callback.onDataLoaded(taskCache!!.values.toList())
                }

                override fun noDataAvailable() {
                    getTaskFromRemoteRepository(callback)
                }
            })
        }
    }


    private fun getTaskFromRemoteRepository(callback: DataLoadCallback<Task>) {
        remoteTaskDataSource.getTasks(object : DataLoadCallback<Task> {
            override fun onDataLoaded(data: List<Task>) {
                refreshCache(data)
                refreshLocalDataSource(data)
                callback.onDataLoaded(data)
            }

            override fun noDataAvailable() = callback.noDataAvailable()
        })
    }


    private fun refreshLocalDataSource(data: List<Task>) {
        localDataSource.deleteTasks {
            data.forEach { task -> localDataSource.saveTask(task) }
        }
    }

    private fun refreshCache(data: List<Task>) {
        if (taskCache == null) taskCache = ConcurrentHashMap()
        (taskCache as MutableMap).let { cache ->
            cache.clear()
            data.withEach { cache[id()] = this }
        }
        cacheIsDirty = true
    }

    override fun getTask(taskId: String, callback: (Task?) -> Unit) {

        val cachedTask = taskCache?.get(taskId)

        if (cachedTask != null) {
            callback(cachedTask)
            return
        }

        localDataSource.getTask(taskId) { task ->
            if (task != null) {
                if (taskCache == null) {
                    taskCache = ConcurrentHashMap()
                }
                (taskCache as MutableMap).put(taskId, task)
            }
            callback(task)
        }
    }

    override fun saveTask(task: Task, completionOrFailure: ((CompletionOrFailure<Task>) -> Unit)?) {
        localDataSource.saveTask(task) {
            remoteTaskDataSource.saveTask(task) {
                if (taskCache == null) taskCache = ConcurrentHashMap()
                (taskCache as ConcurrentHashMap).put(task.id(), task)
                if (completionOrFailure != null) {
                    completionOrFailure(it)
                }
            }
        }

    }

    override fun saveTasks(tasks: List<Task>, completionOrFailure: ((List<CompletionOrFailure<Task>>) -> Unit)?) {

        val callback = object  {
            val collected = LinkedList<CompletionOrFailure<Task>>()
            fun invoke(c:CompletionOrFailure<Task>) {
                if (completionOrFailure != null) {
                    collected.add(c)
                }
            }
        }

        tasks.withEach { saveTask(this, callback::invoke) }
        if (completionOrFailure!=null) {
            completionOrFailure(callback.collected)
        }
    }

    override fun refreshTasks(refreshed: (() -> Unit)?) {
        cacheIsDirty = true
    }

    override fun deleteTasks(completed: (() -> Unit)?) {
        remoteTaskDataSource.deleteTasks()
        localDataSource.deleteTasks()
        taskCache?.let(MutableMap<String, Task>::clear)
        completed?.invoke()
    }

    override fun deleteTask(taskId: String, deleted: ((taskId: String, removedTask: Task?) -> Unit)?) {
        getTask(taskId) { taskToDelete ->
            if (taskToDelete != null) {
                remoteTaskDataSource.deleteTask(taskId)
                localDataSource.deleteTask(taskId)
            }
            deleted?.invoke(taskId, taskToDelete)
        }
    }
}