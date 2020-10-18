package com.example.githubrepo.services

import com.example.githubrepo.services.event.BusEvent
import com.example.githubrepo.services.event.BusEventMapping
import com.squareup.otto.Bus
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Maintains a singleton instance for obtaining the bus. Ideally this would be replaced with a more efficient means
 * such as through injection directly into interested classes.
 */
@Singleton
class BusProvider @Inject constructor(private val bus: Bus) {
    private val busEventHashMap: HashMap<String, Any> = HashMap()
    private val busEventZoneMapping: HashMap<Class<*>, Array<BusEvent.EventType>> = BusEventMapping.zones

    fun reset(mappingZone: Class<*>) {
        val types = busEventZoneMapping[mappingZone]
        for (type in types!!) {
            setHashMap(getType(type))
        }
    }

    fun resetAll() {
        for (eventType in BusEvent.EventType.values()) {
            var event: BusEvent<*>? = null
            try {
                event = getType(eventType)
            } catch (e: IllegalArgumentException) {
            }
            event?.let { setHashMap(it) }
        }
    }

    fun register(obj: Any) {
        bus.register(obj)
    }

    fun unregister(obj: Any) {
        bus.unregister(obj)
    }

    fun post(event: Any) {
        if (busEventZoneMapping.containsKey(event.javaClass)) {
            reset(event.javaClass)
        }
        bus.post(event)
    }

    fun setEvent(event: BusEvent<*>) {
        setHashMap(event)
    }

    fun getEvent(type: BusEvent.EventType): Any? {
        val hashMap = busEventHashMap
        if (hashMap.containsKey(type.name)) {
            return hashMap[type.name]
        }
        val event = getType(type)
        setHashMap(event)
        return event
    }

    private fun getType(type: BusEvent.EventType): BusEvent<*> {
        return type.emptyEvent
    }

    private fun setHashMap(event: BusEvent<*>) {
        busEventHashMap[event.name] = event
    }
}