/*
 * Copyright (c) 2016 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.example.githubrepo;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.githubrepo.models.Repository;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sarahneo on 20/2/17.
 */

public abstract class BaseSearchActivity extends AppCompatActivity {

    private static final String KEY_REPO_LIST = "key_repo_list";

    @BindView(R.id.rv_results) RecyclerView rvResults;
    @BindView(R.id.et_query) EditText etQuery;
    @BindView(R.id.pb_loading) ProgressBar pbLoading;
    @BindView(R.id.btn_search) Button btnSearch;

    protected RepoSearchEngine mRepoSearchEngine;
    private RepoListAdapter mAdapter;
    private List<Repository> mRepoList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mRepoList = new ArrayList<>();

        rvResults.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.setAdapter(mAdapter = new RepoListAdapter(this, mRepoList));

        mRepoSearchEngine = new RepoSearchEngine();
    }

    protected void showProgressBar() {
        pbLoading.setVisibility(View.VISIBLE);
    }

    protected void hideProgressBar() {
        pbLoading.setVisibility(View.GONE);
    }

    protected void showResult(List<Repository> result) {
        if (result.isEmpty()) {
            Toast.makeText(this, R.string.nothing_found, Toast.LENGTH_SHORT).show();
            mRepoList.clear();
            mAdapter.notifyDataSetChanged();
        } else {
            mRepoList.clear();
            mRepoList.addAll(result);
            mAdapter.notifyDataSetChanged();
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(KEY_REPO_LIST, (ArrayList<? extends Parcelable>) mRepoList);
//        outState.putBoolean(KEY_IS_LOADING, mIsLoading);
        super.onSaveInstanceState(outState);
    }

}
