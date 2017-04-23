package ekoatwork.todomvp.support.sqllite

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

inline fun <T> Cursor?.collect(limit: Int = -1, extractFrom: Cursor.() -> T): List<T>? {
    return when {
        (this != null) and (this!!.count > 0) -> mutableListOf<T>().apply {
            if (count > 0) {
                var remaining = limit
                while (moveToNext() and (remaining > 0)) {
                    add(extractFrom(this@collect))
                    --remaining
                }
            }
        }.toList()
        else -> null
    }
}


fun <T> Iterable<T>.withEach(extractFromCursor: T.() -> Unit) {
    forEach(extractFromCursor)
}

fun Cursor.getStringOrNull(column: String): String?  = getString(getColumnIndexOrThrow(column))
fun Cursor.getString(column: String) = getStringOrNull(column) ?: throw NonNullValueRequired("")
fun Cursor.getIntOrNull(column: String):Int? = getColumnIndexOrThrow(column).let { i -> if (isNull(i)) null else getInt(i) }

class NonNullValueRequired(s: String) : Exception(s)


fun <T> SQLiteDatabase.transaction(operation:SQLiteDatabase.()->T): T {
    beginTransaction()
    return try {
        val t = operation(this)
        setTransactionSuccessful()
        t
    } catch (e:Exception) {
        throw e
    } finally {
        endTransaction()
    }
}

