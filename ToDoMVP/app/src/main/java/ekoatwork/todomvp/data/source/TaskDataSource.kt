package ekoatwork.todomvp.data.source

import ekoatwork.todomvp.data.Task

interface DataLoadCallback<in T>  {
    fun onDataLoaded(data: List<T>)
    fun noDataAvailable()
}

sealed class CompletionOrFailure<T> {
    class Completed<T>(val value:T) : CompletionOrFailure<T>()
    class Failed<T>(val value:T?, val cause:Throwable, val message:String): CompletionOrFailure<T>()
}

interface TaskDataSource {
    fun getTasks(callback: DataLoadCallback<Task>)
    fun getTask(taskId: String, callback:(Task?) -> Unit)
    fun saveTask(task: Task, completionOrFailure: ((CompletionOrFailure<Task>) -> Unit)? = null)
    fun saveTasks(tasks: List<Task>, completionOrFailure:((List<CompletionOrFailure<Task>>)->Unit)? = null)
    fun refreshTasks(refreshed:(()->Unit)? = null)
    fun deleteTasks(completed:(()->Unit)? = null)
    fun deleteTask(taskId: String, deleted: ((taskId:String,removedTask:Task?) -> Unit)? = null)
}
