package ekoatwork.todomvp.data.source.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class TaskDbHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        log { "Creating ${TaskPersistenceContract.createTaskTable}" }
        db.execSQL(TaskPersistenceContract.createTaskTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(TaskPersistenceContract.dropTaskTable)
        onCreate(db)
    }

    companion object {
        private const val DB_NAME = "Tasks.db"
        private const val DB_VERSION = 1
        private const val TAG = "TaskDbHelper"
        fun log(message: () -> String) {
            Log.d(TAG, message())
        }
    }

}