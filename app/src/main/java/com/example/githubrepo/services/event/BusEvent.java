package com.example.githubrepo.services.event;

import java.util.Collections;

/**
 * Created by sarah_neo on 22/02/2017.
 */

public abstract class BusEvent<T> {
    public enum EventType {
        REPOS;

        public BusEvent getEmptyEvent() {
            switch (this) {
                case REPOS:
                    return new LoadReposEvent().setData(Collections.EMPTY_LIST);
                default:
                    throw new IllegalArgumentException("No such event registered");
            }
        }
    }

    public abstract BusEvent setData(T data);

    public abstract T getData();

    public abstract EventType getType();

    public String getName() {
        return getType().name();
    }
}
