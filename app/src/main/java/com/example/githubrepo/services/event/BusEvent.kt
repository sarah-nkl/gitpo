package com.example.githubrepo.services.event

abstract class BusEvent<T> {
    enum class EventType {
        REPOS;

        val emptyEvent: BusEvent<*>
            get() = when (this) {
                REPOS -> LoadReposEvent().setData(null)
            }
    }

    abstract fun setData(data: T): BusEvent<*>?
    abstract val data: T
    abstract val type: EventType
    val name: String
        get() = type.name
}