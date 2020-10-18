package com.example.githubrepo.services.event

import java.util.*

object BusEventMapping {
    // Zone effects
    private val RETRIEVE_PRODUCTS = arrayOf(BusEvent.EventType.REPOS)

    //Zone Mapping
    val zones: HashMap<Class<*>, Array<BusEvent.EventType>>
        get() {
            val zones = HashMap<Class<*>, Array<BusEvent.EventType>>()

            //Zone Mapping
            zones[LoadReposEvent::class.java] = RETRIEVE_PRODUCTS
            return zones
        }
}