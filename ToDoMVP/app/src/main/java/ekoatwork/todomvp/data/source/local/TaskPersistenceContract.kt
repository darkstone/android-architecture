package ekoatwork.todomvp.data.source.local

import android.provider.BaseColumns
import android.provider.BaseColumns._ID
import ekoatwork.todomvp.data.Task

object TaskPersistenceContract {

    const val taskTable = "task"

    enum class TaskEntry(val column: String) : BaseColumns {
        ENTRY_ID(_ID),
        TITLE("title"),
        DESCRIPTION("description"),
        COMPLETED("completed")
    }

    const val TEXT = "TEXT"
    const val BOOL = "INT"
    const val NOT_NULL = "NOT NULL"
    const val PK = "primary key"

    val createTableSql: String by lazy { """
            | create table $taskTable
            | (
            |   ${TaskEntry.ENTRY_ID} $TEXT $NOT_NULL $PK,
            |   ${TaskEntry.COMPLETED} $BOOL,
            |   ${TaskEntry.TITLE} $TEXT $NOT_NULL,
            |   ${TaskEntry.DESCRIPTION} $TEXT
            | )
        """.trimMargin()
    }

    const val dropTableSql: String = "DROP TABLE $taskTable"

}