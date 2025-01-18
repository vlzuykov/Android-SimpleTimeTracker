package com.example.util.simpletimetracker.domain.extension

/**
 * Adds item if it not in the list, otherwise removes it from the list.
 */
fun <T> MutableList<T>.addOrRemove(item: T) {
    if (item in this) remove(item) else add(item)
}

fun <T> MutableSet<T>.addOrRemove(item: T) {
    if (item in this) remove(item) else add(item)
}

fun <T, U> MutableMap<T, U>.addOrRemove(item: T, value: U) {
    if (item in this) remove(item) else put(item, value)
}

operator fun <T> MutableCollection<in T>.plusAssign(element: T?) {
    if (element != null) this.add(element)
}

inline fun <T> List<T>.removeIf(crossinline filter: (T) -> Boolean): List<T> {
    return this.toMutableList().apply { removeAll { filter(it) } }
}

inline fun <T> Set<T>.removeIf(crossinline filter: (T) -> Boolean): Set<T> {
    return this.toMutableSet().apply { removeAll { filter(it) } }
}

inline fun <T> List<T>.replaceWith(new: T, crossinline filter: (T) -> Boolean): List<T> {
    return this.removeIf(filter).toMutableList().apply { add(new) }
}