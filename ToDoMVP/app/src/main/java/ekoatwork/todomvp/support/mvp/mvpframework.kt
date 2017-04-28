package ekoatwork.todomvp.support.mvp

interface BasePresenter {
    fun start()
}

interface BaseView<in T: BasePresenter> {
    fun setPresenter(presenter: T)
}