package ekoatwork.todomvp.support.mvp

interface Presenter {
    fun start()
}

interface BaseView<in T:Presenter> {
    fun setPresenter(presenter: T)
}