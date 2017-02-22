package com.example.githubrepo.services.event;

import com.example.githubrepo.models.Repository;

import java.util.List;

/**
 * Created by sarah_neo on 22/02/2017.
 */

public class LoadReposEvent extends BusEvent<List<Repository>> {

    private List<Repository> mRepoList;

    @Override
    public BusEvent setData(List<Repository> data) {
        mRepoList = data;
        return this;
    }

    @Override
    public List<Repository> getData() {
        return mRepoList;
    }

    @Override
    public EventType getType() {
        return EventType.REPOS;
    }
}
