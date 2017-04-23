package ekoatwork.todomvp.support


fun <T> Iterable<T>.withEach(apply:T.()->Unit) {
    forEach(apply)
}