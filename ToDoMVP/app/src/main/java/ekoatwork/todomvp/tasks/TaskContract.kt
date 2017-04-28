package ekoatwork.todomvp.tasks

import android.content.Intent
import ekoatwork.todomvp.data.Task
import ekoatwork.todomvp.support.mvp.BasePresenter
import ekoatwork.todomvp.support.mvp.BaseView

interface TaskContract {

    interface Presenter : BasePresenter {
        fun loadTasks(forceUpdate: Boolean)
        fun addNewTask()
        fun openTaskDetails(task: Task)
        fun completeTask(task: Task)
        fun activateTask(task: Task)
        var taskFilter: TasksFilterType
        fun result(requestCode: Int, resultCode: Int, data: Intent?)
        fun clearCompletedTasks()
    }

    interface View : BaseView<Presenter> {
        fun setLoadingIndicator(busyLoading: Boolean)
        fun showTasks(tasks: List<Task>)
        fun showAddTask()
        fun showTasksDetailsUi(taskId: String)
        fun showTasksMarkedCompleted()
        fun showTasksMarkedActive()
        fun showTasksCleared()
        fun showLoadingTaskError()
        fun showActiveFilterLabel()
        fun showCompletedFilterLabel()
        fun showNoActiveTasks()
        fun showAllFilterLabel()
        val isActive: Boolean
        fun showNoCompletedTasks()
        fun showNoTasks()
        fun showSuccessfullySavedMessages()
    }
}