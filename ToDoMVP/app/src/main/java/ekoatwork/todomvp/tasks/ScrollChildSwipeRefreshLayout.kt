package ekoatwork.todomvp.tasks

import android.content.Context
import android.support.v4.view.ViewCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.util.AttributeSet
import android.view.View

class ScrollChildSwipeRefreshLayout(context: Context, attributeSet: AttributeSet?) : SwipeRefreshLayout(context, attributeSet) {

    constructor(context: Context) : this(context, null)

    var scrollChildUpView: View? = null

    override fun canChildScrollUp(): Boolean
            = scrollChildUpView?.let { child -> ViewCompat.canScrollVertically(child, -1) } ?: super.canChildScrollUp()
}