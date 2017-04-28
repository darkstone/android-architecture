package ekoatwork.todomvp.support.android

import android.app.Activity
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

inline fun <reified T : View> View.viewById(id: Int): T = findViewById(id) as T



inline fun <reified T : View> View.queryViewById(id: Int): T? = findViewById(id) as T?
inline fun <reified T : View> View.withView(id: Int, apply:T.()->Unit) {
    viewById<T>(id).apply(apply)
}

inline fun <reified T : View> Activity.withView(id: Int, apply: T.() -> Unit) {
    (findViewById(id) as T).apply(apply)
}

fun ViewGroup.inflate(viewId: Int, attachToRoot: Boolean = false) = LayoutInflater.from(context).inflate(viewId, this, attachToRoot)!!


inline fun <reified T : View> Activity.viewById(id: Int): T = (findViewById(id) as T)!!
inline fun <reified T : View> Activity.queryViewById(id: Int): T? = findViewById(id) as T

fun AppCompatActivity.addFragment(fragment: Fragment, frameId: Int) {
    supportFragmentManager.beginTransaction().add(frameId, fragment).commit()
}