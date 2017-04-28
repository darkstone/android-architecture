package ekoatwork.todomvp.tasks

import android.app.Activity
import android.content.Intent
import ekoatwork.todomvp.addedittask.AddEditTaskActivity

import ekoatwork.todomvp.data.Task
import ekoatwork.todomvp.data.id
import ekoatwork.todomvp.data.isActive
import ekoatwork.todomvp.data.source.DataLoadCallback
import ekoatwork.todomvp.data.source.TaskRepository

class TaskPresenter(private val taskRepository: TaskRepository,
                    private val view: TaskContract.View) : TaskContract.Presenter {

    init {
        view.setPresenter(this)
    }

    override var taskFilter: TasksFilterType = TasksFilterType.ALL_TASKS
    private var firstLoad = true

    override fun start() {
        loadTasks(false)
    }

    override fun loadTasks(forceUpdate: Boolean) {
        loadTasks(forceUpdate or firstLoad,  true)
        firstLoad = false
    }

    private fun loadTasks(forceUpdate: Boolean, showLoadingUI: Boolean) {
        if (showLoadingUI) view.setLoadingIndicator(true)
        if (forceUpdate) taskRepository.refreshTasks()
        taskRepository.getTasks(object : DataLoadCallback<Task> {
            override fun onDataLoaded(data: List<Task>) {
                if (view.isActive) {
                    val tasks = data.filter {
                        when (taskFilter) {
                            TasksFilterType.ACTIVE_TASKS -> it.isActive
                            TasksFilterType.COMPLETED_TASKS -> it.completed
                            else -> true
                        }
                    }
                    processTasks(tasks)
                }
                if (showLoadingUI) view.setLoadingIndicator(false)
            }

            override fun noDataAvailable() {
                if (view.isActive) view.showLoadingTaskError()
            }
        })
    }

    private fun processTasks(tasks: List<Task>) {
        if (tasks.isEmpty()) {
            processEmptyTasks()
        } else {
            view.showTasks(tasks)
            showFilterLabel()
        }
    }

    private fun showFilterLabel() {
        with(view) {
            when (taskFilter) {
                TasksFilterType.COMPLETED_TASKS -> showCompletedFilterLabel()
                TasksFilterType.ACTIVE_TASKS -> showActiveFilterLabel()
                else -> showAllFilterLabel()
            }
        }
    }

    private fun processEmptyTasks() {
        with(view) {
            when (taskFilter) {
                TasksFilterType.ACTIVE_TASKS -> showNoActiveTasks()
                TasksFilterType.COMPLETED_TASKS -> showNoCompletedTasks()
                else -> showNoTasks()
            }
        }
    }

    override fun addNewTask() {
        view.showAddTask()
    }

    override fun openTaskDetails(task: Task) {
        view.showTasksDetailsUi(task.id())
    }

    override fun completeTask(task: Task) {
        task.completed = true
        taskRepository.saveTask(task) {
            view.showTasksMarkedCompleted()
            loadTasks(false, false)
        }
    }

    override fun activateTask(task: Task) {
        task.completed = true
        taskRepository.saveTask(task) {
            view.showTasksMarkedActive()
            loadTasks(false, false)
        }
    }

    override fun result(requestCode: Int, resultCode: Int, data: Intent?) {
        if ((AddEditTaskActivity.REQUEST_ADD_TASK == requestCode) and (Activity.RESULT_OK == resultCode)) {
            view.showSuccessfullySavedMessages()
        }
    }

    override fun clearCompletedTasks() {
        taskRepository.deleteTasks(TasksFilterType.COMPLETED_TASKS) {loadTasks(false, false)}
    }
}