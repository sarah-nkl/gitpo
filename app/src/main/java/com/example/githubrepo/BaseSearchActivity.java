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

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.githubrepo.models.Repository;
import com.example.githubrepo.services.BusProvider;
import com.example.githubrepo.services.event.BusEvent;
import com.example.githubrepo.services.event.LoadReposEvent;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by sarahneo on 20/2/17.
 */

public abstract class BaseSearchActivity extends AppCompatActivity {

    private static final String KEY_REPO_LIST = "key_repo_list";
    private static final String KEY_IS_LOADING = "key_is_loading";
    private static final String KEY_IS_SHOW_SEARCH = "key_is_show_search";
    private static final int VISIBLE_THRESHOLD = 5;

    @BindView(R.id.rv_results) RecyclerView rvResults;
    @BindView(R.id.et_query) EditText etQuery;
    @BindView(R.id.pb_loading) ProgressBar pbLoading;
    @BindView(R.id.btn_back) ImageButton btnBack;

    protected MenuItem menuItemSearch;
    protected Drawable x;

    protected RepoSearchEngine mRepoSearchEngine;
    private RepoListAdapter mAdapter;
    protected List<Repository> mRepoList;
    private LinearLayoutManager mLayoutManager;
    private boolean mIsShowSearch = false;
    protected boolean mIsLoading = false;

    @Inject
    BusProvider busProvider;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        ((MyApplication) getApplication()).inject(this);

        initToolbar();
        initEditText();

        mRepoList = new ArrayList<>();

        LoadReposEvent event = (LoadReposEvent) getEvent(BusEvent.EventType.REPOS);
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_REPO_LIST)) {
            mRepoList = savedInstanceState.getParcelableArrayList(KEY_REPO_LIST);
            mIsLoading = savedInstanceState.getBoolean(KEY_IS_LOADING);
            mIsShowSearch = savedInstanceState.getBoolean(KEY_IS_SHOW_SEARCH);
        } else if (event.getData().size() != 0)
            mRepoList.addAll(event.getData());

        mLayoutManager = new LinearLayoutManager(this);
        rvResults.setLayoutManager(mLayoutManager);
        rvResults.setAdapter(mAdapter = new RepoListAdapter(this, mRepoList));
        rvResults.addOnScrollListener(mOnScrollListener);

        mRepoSearchEngine = new RepoSearchEngine();
    }

    private void initToolbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    private void initEditText() {
        etQuery.requestFocus();
        x = getResources().getDrawable(R.drawable.ic_clear_white_24dp);

        x.setBounds(0, 0, x.getIntrinsicWidth(), x.getIntrinsicHeight());
        etQuery.setCompoundDrawables(null, null, etQuery.getText().toString().equals("") ? null : x, null);
        etQuery.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                if (etQuery.getCompoundDrawables()[2] == null) {
                    return false;
                }
                if (event.getAction() != MotionEvent.ACTION_UP) {
                    return false;
                }
                if (event.getX() > etQuery.getWidth() - etQuery.getPaddingRight() - x.getIntrinsicWidth()) {

                    etQuery.setText("");
                    etQuery.setCompoundDrawables(null, null, null, null);
                }
                return false;
            }
        });
    }

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            int totalItemCount = mLayoutManager.getItemCount();
            int lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();

            if (!mIsLoading && totalItemCount <= (lastVisibleItem + VISIBLE_THRESHOLD)) {
                //onLoadMore();
                mIsLoading = true;
            }
        }
    };

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menuItemSearch = menu.findItem(R.id.search_shortcut);
        toggleShowSearch(mIsShowSearch);


        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.search_shortcut:
                toggleShowSearch(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.btn_back)
    public void clickBack() {
        toggleShowSearch(false);
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

    private void toggleShowSearch(boolean isShow) {
        if (isShow) {
            menuItemSearch.setVisible(false);
            etQuery.setVisibility(View.VISIBLE);
            btnBack.setVisibility(View.VISIBLE);
            etQuery.requestFocus();
        } else {
            etQuery.setVisibility(View.GONE);
            btnBack.setVisibility(View.GONE);
            menuItemSearch.setVisible(true);
        }
    }

//    private void onLoadMore() {
//        if (!isAllLoaded) {
//            // Load data
//            int index = mRepoList.size();
//            get(index / RepoSearchEngine.PER_PAGE);
//        }
//    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(KEY_REPO_LIST, (ArrayList<? extends Parcelable>) mRepoList);
        outState.putBoolean(KEY_IS_LOADING, mIsLoading);
        outState.putBoolean(KEY_IS_SHOW_SEARCH, !menuItemSearch.isVisible());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            busProvider.register(this);
        } catch (IllegalArgumentException e) {}
    }

    @Override
    protected void onStop() {
        super.onStop();
        busProvider.unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LoadReposEvent event = (LoadReposEvent) getEvent(
                BusEvent.EventType.REPOS);
        event.setData(mRepoList);

        rvResults.removeOnScrollListener(mOnScrollListener);
    }

    public void post(Object event) {
        busProvider.post(event);
    }

    public Object getEvent(BusEvent.EventType type) {
        return busProvider.getEvent(type);
    }

}
