package ekoatwork.todomvp.tasks

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import ekoatwork.todomvp.R
import ekoatwork.todomvp.addedittask.AddEditTaskActivity
import ekoatwork.todomvp.data.Task
import ekoatwork.todomvp.support.android.inflate
import ekoatwork.todomvp.support.android.viewById
import ekoatwork.todomvp.support.android.withView
import ekoatwork.todomvp.taskdetail.TaskDetailActivity


class TasksFragment : Fragment(), TaskContract.View {
    private val taskListener = object : TaskAdapter.TaskListener {

        override fun onTaskClicked(task: Task) {
            presenter.openTaskDetails(task)
        }

        override fun onCompleteTaskClicked(task: Task) {
            presenter.completeTask(task)
        }

        override fun onActivateTaskClicked(task: Task) {
            presenter.activateTask(task)
        }

    }

    private lateinit var presenter: TaskContract.Presenter
    private lateinit var taskListAdapter: TaskAdapter
    private lateinit var noTasksView: View
    private lateinit var noTaskIcon: ImageView
    private lateinit var noTaskMainView: TextView
    private lateinit var noTaskAddView: TextView
    private lateinit var tasksView: LinearLayout
    private lateinit var filteringLabelView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskListAdapter = TaskAdapter(emptyList(), taskListener)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.tasks_frag, container, false).apply {

            noTasksView = viewById(R.id.noTasks)
            noTaskIcon = viewById(R.id.noTasksIcon)
            noTaskMainView = viewById(R.id.noTasksMain)
            noTaskAddView = viewById(R.id.noTasksAdd)
            noTaskAddView.setOnClickListener { showAddTask() }

            viewById<ListView>(R.id.tasks_list).adapter = taskListAdapter


            // setup FAB
            activity.withView<FloatingActionButton>(R.id.fab_add_task) {
                setImageResource(R.drawable.ic_add)
                setOnClickListener {
                    presenter.addNewTask()
                }
            }
            // setup progress indicator
            withView<ScrollChildSwipeRefreshLayout>(R.id.refresh_layout) {
                setColorSchemeColors(
                        ContextCompat.getColor(activity, R.color.colorPrimary),
                        ContextCompat.getColor(activity, R.color.colorAccent),
                        ContextCompat.getColor(activity, R.color.colorPrimaryDark))
                scrollChildUpView = this@apply.viewById(R.id.tasks_list)
                setOnRefreshListener { presenter.loadTasks(false) }
                setHasOptionsMenu(true)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.menu_clear -> presenter.clearCompletedTasks()
            R.id.menu_filter -> showFilteringPopupMenu()
            R.id.menu_refresh -> presenter.loadTasks(true)
        }
        return true
    }

    private fun showFilteringPopupMenu() {
        PopupMenu(context, activity.findViewById(R.id.menu_filter)).apply {
            menuInflater.inflate(R.menu.filter_tasks, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.active -> presenter.taskFilter = TasksFilterType.ACTIVE_TASKS
                    R.id.completed -> presenter.taskFilter = TasksFilterType.COMPLETED_TASKS
                    else -> presenter.taskFilter = TasksFilterType.ALL_TASKS
                }
                true
            }
            show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presenter.result(requestCode, resultCode, data)
    }

    class TaskAdapter(private var tasks: List<Task>, private val taskListener: TaskListener) : BaseAdapter() {

        interface TaskListener {
            fun onTaskClicked(task: Task)
            fun onCompleteTaskClicked(task: Task)
            fun onActivateTaskClicked(task: Task)
        }

        override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup): View {
            val rowView = convertView ?: viewGroup.inflate(R.layout.task_item)
            rowView.bindTask(getItem(position))
            return rowView
        }

        override fun getItem(position: Int): Task = tasks[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getCount(): Int = tasks.size

        private fun View.bindTask(task: Task) {
            if (tag != task) {
                tag = task
                withView<TextView>(R.id.title) { text = task.title }
                withView<CheckBox>(R.id.completed) {
                    isChecked = task.completed
                    setOnCheckedChangeListener { buttonView, isChecked ->
                        if (task.completed) {
                            taskListener.onCompleteTaskClicked(task)
                        } else {
                            taskListener.onActivateTaskClicked(task)
                        }
                    }
                }

                background = resources.getDrawable(
                        if (task.completed)
                            R.drawable.list_completed_touch_feedback
                        else
                            R.drawable.touch_feedback)

                setOnClickListener { taskListener.onTaskClicked(task) }
            }
        }

        fun replaceData(tasks: List<Task>) {
            this.tasks = tasks
            notifyDataSetChanged()
        }
    }

    override fun setPresenter(presenter: TaskContract.Presenter) {
        this.presenter = presenter
    }

    override fun setLoadingIndicator(busyLoading: Boolean) {
        view?.apply {
            withView<SwipeRefreshLayout>(R.id.refresh_layout) {
                post {
                    isRefreshing = busyLoading
                }
            }
        }
    }

    override fun showTasks(tasks: List<Task>) {
        taskListAdapter.replaceData(tasks)
        tasksView.visibility = View.VISIBLE
        noTasksView.visibility = View.GONE
    }

    override fun showNoActiveTasks() {
        showNoTasksView(resources.getString(R.string.no_tasks_active), R.drawable.ic_check_circle_24dp, false)
    }

    private fun showNoTasksView(message: String, resId: Int, showAddView: Boolean) {
        tasksView.visibility = View.GONE
        noTasksView.visibility = View.VISIBLE
        //noTasksView.text = message
        noTaskIcon.setImageDrawable(resources.getDrawable(resId))
        noTaskAddView.visibility = if (showAddView) View.VISIBLE else View.GONE
    }

    override fun showAddTask() {
        Intent(context, AddEditTaskActivity::class.java).apply {
            startActivityForResult(this, AddEditTaskActivity.REQUEST_ADD_TASK)
        }
    }

    override fun showTasksDetailsUi(taskId: String) {
        // in it's own Activity, since it makes more sense that way and it gives us the flexibility
        // to show some Intent stubbing.
        Intent(context, TaskDetailActivity::class.java).apply {
            putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskId)
            startActivity(this)
        }
    }

    override fun showNoCompletedTasks() {
        showMessage(getString(R.string.no_tasks_completed))
    }

    override fun showNoTasks() {
        showMessage(getString(R.string.no_tasks_all))
    }

    override fun showTasksMarkedCompleted() {
        showMessage(getString(R.string.task_marked_complete))
    }

    override fun showTasksMarkedActive() {
        showMessage(getString(R.string.task_marked_active));
    }

    override fun showTasksCleared() {
        showMessage(getString(R.string.completed_tasks_cleared));
    }

    override fun showLoadingTaskError() {
        showMessage(getString(R.string.loading_tasks_error));
    }

    override fun showActiveFilterLabel() {
        filteringLabelView.setText(R.string.label_active)
    }

    override fun showCompletedFilterLabel() {
        filteringLabelView.setText(R.string.label_completed)
    }

    override fun showAllFilterLabel() {
        filteringLabelView.setText(R.string.label_all)
    }


    override fun showSuccessfullySavedMessages() {
        showMessage(getString(R.string.successfully_saved_task_message))
    }

    private fun showMessage(message: String) {
        Snackbar.make(view!!, message, Snackbar.LENGTH_LONG).show()
    }

    override val isActive: Boolean
        get() = isAdded

    companion object {
        fun newInstance(): TasksFragment = TasksFragment()
    }

}