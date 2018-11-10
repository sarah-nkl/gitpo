package com.example.githubrepo.services.event;

import java.util.HashMap;

import static com.example.githubrepo.services.event.BusEvent.EventType;

/**
 * Created by sarah_neo on 22/02/2017.
 */

public class BusEventMapping {
    // Zone effects

    public static EventType[] RETRIEVE_PRODUCTS = {EventType.REPOS};

    public static HashMap<Class, EventType[]> getZones() {
        HashMap<Class, EventType[]> zones = new HashMap<>();

        //Zone Mapping
        zones.put(LoadReposEvent.class, RETRIEVE_PRODUCTS);

        return zones;
    }
}
