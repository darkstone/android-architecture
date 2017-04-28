package ekoatwork.todomvp.tasks

import android.os.Bundle
import android.os.PersistableBundle
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.MenuItem
import ekoatwork.todomvp.Injection
import ekoatwork.todomvp.R
import ekoatwork.todomvp.support.android.addFragment
import ekoatwork.todomvp.support.android.queryViewById
import ekoatwork.todomvp.support.android.viewById

class TaskActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var presenter: TaskContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tasks_act)

        // setup toolbar
        setSupportActionBar(viewById(R.id.toolbar))
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Setup Navigation drawer
        drawerLayout = viewById(R.id.drawer_layout)
        drawerLayout.setStatusBarBackground(R.color.colorPrimaryDark)
        queryViewById<NavigationView>(R.id.nav_view)?.setupDrawContent()

        val taskFragment = supportFragmentManager
                .findFragmentById(R.id.contentFrame)
                ?.let { it as TasksFragment }
                ?: TasksFragment.newInstance().apply {
            addFragment(this, R.id.contentFrame)
        }

        presenter = TaskPresenter(Injection(applicationContext).taskRepository, taskFragment)
        presenter.taskFilter = savedInstanceState?.get(CURRENT_FILTERING_KEY)?.let { it as TasksFilterType } ?: TasksFilterType.ALL_TASKS
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle?) {
        outState.putSerializable(CURRENT_FILTERING_KEY, presenter.taskFilter)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(Gravity.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun NavigationView.setupDrawContent() {
        setNavigationItemSelectedListener { item ->
            drawerLayout.closeDrawers()
            item.isChecked = true
            true
        }
    }

    companion object {
        private const val CURRENT_FILTERING_KEY = "CURRENT_FILTERING_KEY"
    }
}

