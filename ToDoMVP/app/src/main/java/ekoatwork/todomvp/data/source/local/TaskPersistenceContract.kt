package ekoatwork.todomvp.data.source.local

import android.provider.BaseColumns
import android.provider.BaseColumns._ID

object TaskPersistenceContract {

    const val taskTable = "task"

    enum class Column(val column: String) : BaseColumns {
        ENTRY_ID(_ID),
        TITLE("title"),
        DESCRIPTION("description"),
        COMPLETED("completed")
    }

    const val TEXT = "TEXT"
    const val BOOL = "INT"
    const val NOT_NULL = "NOT NULL"
    const val PK = "primary key"

    val createTaskTable: String by lazy { """
            | create table $taskTable
            | (
            |   ${Column.ENTRY_ID.column} $TEXT $NOT_NULL $PK,
            |   ${Column.COMPLETED.column} $BOOL,
            |   ${Column.TITLE.column} $TEXT $NOT_NULL,
            |   ${Column.DESCRIPTION.column} $TEXT
            | )
        """.trimMargin()
    }

    const val dropTaskTable: String = "DROP TABLE IF EXISTS $taskTable"

}