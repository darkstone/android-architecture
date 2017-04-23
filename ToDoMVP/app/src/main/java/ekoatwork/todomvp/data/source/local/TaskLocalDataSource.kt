package ekoatwork.todomvp.data.source.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import ekoatwork.todomvp.data.Task
import ekoatwork.todomvp.data.id
import ekoatwork.todomvp.data.source.CompletionOrFailure
import ekoatwork.todomvp.data.source.DataLoadCallback
import ekoatwork.todomvp.data.source.TaskDataSource
import ekoatwork.todomvp.data.source.local.TaskPersistenceContract.TaskEntry
import ekoatwork.todomvp.data.source.local.TaskPersistenceContract.TaskEntry.*
import ekoatwork.todomvp.support.sqllite.*

class TaskLocalDataSource private constructor(context: Context) : TaskDataSource {

    private val taskDbHelper: TaskDbHelper = TaskDbHelper(context)
    private val projection = TaskEntry.values().map(TaskEntry::column).toTypedArray()

    override fun getTasks(callback: DataLoadCallback<Task>) {
        val tasks = taskDbHelper.readableDatabase.use { db ->
            db.query(TaskPersistenceContract.taskTable, projection, null, null, null, null, null, null).use { cursor ->
                cursor.collect { toTask() }
            }
        }
        tasks?.takeIf { it.isNotEmpty() }?.let { callback.onDataLoaded(it) } ?: callback.noDataAvailable()
    }

    private fun Cursor.toTask(): Task {
        return Task(
                id = getString(ENTRY_ID.column),
                title = getString(TITLE.column),
                description = getStringOrNull(DESCRIPTION.column),
                completed = getIntOrNull(COMPLETED.column)?.let { it > 0 } ?: false)
    }

    override fun getTask(taskId: String, callback: (Task?) -> Unit) {
        val task = taskDbHelper.readableDatabase.use { db ->
            with(TaskPersistenceContract) {
                val where = "(${ENTRY_ID.column} = ?)"
                val args = arrayOf(taskId)
                db.query(taskTable, projection, where, args, null, null, null).collect(1) { toTask() }
            }
        }?.firstOrNull()

        callback(task)
    }

    override fun saveTask(task: Task, completionOrFailure: ((CompletionOrFailure<Task>) -> Unit)?) {
        val result = taskDbHelper.writableDatabase.use { db ->
            db.transaction {
                with(TaskPersistenceContract) {
                    val values: ContentValues by lazy {
                        ContentValues().apply {
                            put(ENTRY_ID.column, task.id())
                            put(DESCRIPTION.column, task.description)
                            put(TITLE.column, task.title)
                            put(COMPLETED.column, if (task.completed) 1 else 0)
                        }
                    }

                    if (task.id == null) {
                        db.insert(taskTable, null, values)
                        CompletionOrFailure.Completed(task)
                    } else {
                        db.update(taskTable, values, "${ENTRY_ID.column}=?", arrayOf(task.id)).toLong()
                        CompletionOrFailure.Completed(task)
                    }
                }
            }
        }
        completionOrFailure?.let { callback -> callback(result) }
    }

    override fun saveTasks(tasks: List<Task>, completionOrFailure: ((List<CompletionOrFailure<Task>>) -> Unit)?) {
        val results = mutableListOf<CompletionOrFailure<Task>>()
        tasks.forEach { task ->
            saveTask(task) { result ->
                results.add(result)
            }
        }
        if (completionOrFailure!=null) completionOrFailure(results)
    }

    override fun refreshTasks(refreshed: (() -> Unit)?) {
        // Handled else where
    }

    override fun deleteTasks(completed: (() -> Unit)?) {
        taskDbHelper.writableDatabase.use { db -> db.delete(TaskPersistenceContract.taskTable, null, null) }
        completed?.invoke()
    }

    override fun deleteTask(taskId: String, deleted: ((taskId: String, removedTask: Task?) -> Unit)?) {
        with(TaskPersistenceContract) {

            val deleteTask = {
                taskDbHelper.writableDatabase.use { db ->
                    db.transaction {
                        delete(taskTable, "${ENTRY_ID.column} = ?", arrayOf(taskId))
                    }
                }
            }

            if (deleted == null) {
                deleteTask()
            } else {
                val deletableTask by lazy {
                    var task : Task? = null
                    getTask(taskId) {
                        if (it != null) {
                            task = it
                            task!!.id = null
                            deleteTask()
                        }
                    }
                    task
                }
                deleted(taskId, deletableTask)
            }
        }
    }

    companion object {
        private const val _lock = 1
        private var taskLocalDataSource: TaskLocalDataSource? = null
        operator fun invoke(context: Context): TaskLocalDataSource {
            return synchronized(_lock) {
                if (taskLocalDataSource == null) taskLocalDataSource = TaskLocalDataSource(context)
                taskLocalDataSource as TaskLocalDataSource
            }
        }
    }
}