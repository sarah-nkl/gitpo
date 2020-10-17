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

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.githubrepo.models.Repository;
import com.example.githubrepo.services.event.BusEvent;
import com.example.githubrepo.services.event.LoadReposEvent;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by sarahneo on 20/2/17.
 */

public class MainActivity extends BaseSearchActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String KEY_REPO_LIST = "key_repo_list";
    private static final String KEY_IS_LOADING = "key_is_loading";
    private static final String KEY_IS_SHOW_SEARCH = "key_is_show_search";
    private static final String KEY_IS_ALL_LOADED = "key_is_all_loaded";
    private static final String SORT_BY_STARS = "stars";
    private static final String SORT_BY_UPDATED = "updated";
//    private static final String SORT_BY_FORKS = "forks";
    private static final String QUERY_ON_FIRST_LOAD = "stars:>5000";
    private static final int VISIBLE_THRESHOLD = 5;

    String mNoHistMsg;

    private RepoListAdapter mAdapter;
    private List<Repository> mRepoList;
    private GridLayoutManager mLayoutManager;
    private boolean mIsLoading = false;
    private boolean mIsAllLoaded = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mNoHistMsg = getResources().getString(R.string.no_search_history_msg);
        initToolbar();
        initEditText();
        initButtons();

        mRepoList = new ArrayList<>();

        LoadReposEvent event = (LoadReposEvent) getEvent(BusEvent.EventType.REPOS);
        if (savedInstanceState != null) {
            mRepoList = savedInstanceState.getParcelableArrayList(KEY_REPO_LIST);
            mIsLoading = savedInstanceState.getBoolean(KEY_IS_LOADING);
            mIsAllLoaded = savedInstanceState.getBoolean(KEY_IS_ALL_LOADED);
            toggleShowSearch(savedInstanceState.getBoolean(KEY_IS_SHOW_SEARCH));
        } else if (event.getData().size() != 0)
            mRepoList.addAll(event.getData());

        mLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.grid_item_list_count));
        mBinding.rvResults.setLayoutManager(mLayoutManager);
        mBinding.rvResults.setAdapter(mAdapter = new RepoListAdapter(this, mRepoList));

        if (mRepoList.size() == 0 && !mIsLoading) {
            // Check last search
            String lastSearch = mSharedPref.getString(Constants.SP_LAST_QUERY, "");
            if (lastSearch.equals("")) {
                // On first load, generate popular repositories
                getRepoList(QUERY_ON_FIRST_LOAD, SORT_BY_UPDATED, 1);
                Snackbar snackbar = Snackbar
                        .make(mBinding.coordinatorLayout, mNoHistMsg, Snackbar.LENGTH_LONG);

                snackbar.show();
            } else {
                getRepoList(lastSearch, SORT_BY_STARS, 1);
                mBinding.etQuery.setText(lastSearch);
                mBinding.etQuery.setSelection( mBinding.etQuery.getText().length());
            }
        }

        mBinding.etQuery.setCompoundDrawables(null, null,  mBinding.etQuery.getText().toString().equals("") ? null : x, null);
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initEditText() {
        mBinding.etQuery.requestFocus();
        x = ContextCompat.getDrawable(this, R.drawable.ic_clear_white_24dp);

        x.setBounds(0, 0, x.getIntrinsicWidth(), x.getIntrinsicHeight());
        mBinding.etQuery.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                if ( mBinding.etQuery.getCompoundDrawables()[2] == null) {
                    return false;
                }
                if (event.getAction() != MotionEvent.ACTION_UP) {
                    return false;
                }
                if (event.getX() >  mBinding.etQuery.getWidth() -  mBinding.etQuery.getPaddingRight() - x.getIntrinsicWidth()) {

                    mBinding.etQuery.setText("");
                    mBinding.etQuery.setCompoundDrawables(null, null, null, null);
                }
                return false;
            }
        });
    }

    private void initButtons() {
        mBinding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleShowSearch(false);
            }
        });

        mBinding.btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleShowSearch(true);
            }
        });
    }

    private void initToolbar() {
        setSupportActionBar(mBinding.toolbar);
    }

    private void toggleShowSearch(boolean isShow) {
        if (isShow) {
            mBinding.btnSearch.setVisibility(View.GONE);
            mBinding.etQuery.setVisibility(View.VISIBLE);
            mBinding.btnBack.setVisibility(View.VISIBLE);
            mBinding. etQuery.requestFocus();
        } else {
            mBinding.etQuery.setVisibility(View.GONE);
            mBinding.btnBack.setVisibility(View.GONE);
            mBinding.btnSearch.setVisibility(View.VISIBLE);
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(KEY_REPO_LIST, (ArrayList<? extends Parcelable>) mRepoList);
        outState.putBoolean(KEY_IS_LOADING, mIsLoading);
        outState.putBoolean(KEY_IS_ALL_LOADED, mIsAllLoaded);
        outState.putBoolean(KEY_IS_SHOW_SEARCH, mBinding.btnSearch.getVisibility() == View.GONE);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Add listeners
        mBinding.etQuery.addTextChangedListener(mTextWatcher);
        mBinding.etQuery.setOnEditorActionListener(mEditorActionListener);
        mBinding.rvResults.addOnScrollListener(mOnScrollListener);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsLoading)
            showProgressBar();
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideProgressBar();
    }

    @Subscribe
    public void OnLoadReposEvent(LoadReposEvent event) {
        hideProgressBar();

        if (event.getData() == null || event.getData().size() == 0) {
            mIsAllLoaded = true;
            if (mRepoList.size() == 0) {
                // No results found
                if (mBinding.tvNoResults.getVisibility() == View.GONE)
                    mBinding.tvNoResults.setVisibility(View.VISIBLE);
            }

        } else {
            if (mBinding.tvNoResults.getVisibility() == View.VISIBLE)
                mBinding.tvNoResults.setVisibility(View.GONE);
            int totalCount = event.getData().get(0).getTotal();
            int posStart = mRepoList.size();
            mRepoList.addAll(event.getData());
            mAdapter.notifyItemRangeInserted(posStart, event.getData().size());
            mIsLoading = false;
            if (mRepoList.size() == totalCount)
                mIsAllLoaded = true;
        }
    }

    private void getRepoList(String query, String sort, int pageNum) {
        showProgressBar();
        mIsLoading = true;

        Call<List<Repository>> mCallProductList = mService.listRepos(query, sort, null, pageNum,
                Constants.NUM_LOADED);


        mCallProductList.enqueue(new Callback<List<Repository>>() {
            @Override
            public void onResponse(@NonNull Call<List<Repository>> call, @NonNull Response<List<Repository>> response) {
                LoadReposEvent event = (LoadReposEvent) getEvent(
                        BusEvent.EventType.REPOS);
                event.setData(response.body());
                post(event);
            }

            @Override
            public void onFailure(@NonNull Call<List<Repository>> call, @NonNull Throwable t) {
                hideProgressBar();
                Log.d(TAG, t.getMessage());
                t.printStackTrace();
                mRepoList.clear();
                mAdapter.notifyDataSetChanged();
                mBinding.tvNoResults.setVisibility(View.VISIBLE);
            }
        });
    }

    private final TextWatcher mTextWatcher =  new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mBinding.etQuery.setCompoundDrawables(
                    null,
                    null,
                    mBinding.etQuery.getText().toString().equals("") ? null : x, null);

        }
    };

    private final TextView.OnEditorActionListener mEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                mRepoList.clear();
                mIsAllLoaded = false;
                getRepoList(mBinding.etQuery.getText().toString(), SORT_BY_STARS, 1);
                hideSoftKeyboard();
                return true;
            }
            return false;
        }
    };

    final private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            int totalItemCount = mLayoutManager.getItemCount();
            int lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();

            if (dy != 0 && !mIsLoading && totalItemCount <= (lastVisibleItem + VISIBLE_THRESHOLD)) {
                onLoadMore();
            }
        }
    };

    private void onLoadMore() {
        if (!mIsAllLoaded && ! mBinding.etQuery.getText().toString().equals("")) {
            // Load data
            int index = mRepoList.size();
            getRepoList( mBinding.etQuery.getText().toString(), SORT_BY_STARS, index / Constants.NUM_LOADED + 1);
        }
    }

    private void showProgressBar() {
        mBinding.pbLoading.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        mBinding.pbLoading.setVisibility(View.GONE);
    }

    private void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Remove listeners
        mBinding.etQuery.removeTextChangedListener(mTextWatcher);
        mBinding.etQuery.setOnEditorActionListener(null);
        mBinding.rvResults.removeOnScrollListener(mOnScrollListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LoadReposEvent event = (LoadReposEvent) getEvent(
                BusEvent.EventType.REPOS);
        event.setData(mRepoList);

        mSharedPref.edit().putString(Constants.SP_LAST_QUERY,  mBinding.etQuery.getText().toString()).apply();
    }

}
