package ekoatwork.todomvp.data.source.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TaskDbHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(TaskPersistenceContract.createTableSql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(TaskPersistenceContract.dropTableSql)
    }

    companion object {
        private const val DB_NAME = "Tasks.db"
        private const val DB_VERSION = 1
    }

}