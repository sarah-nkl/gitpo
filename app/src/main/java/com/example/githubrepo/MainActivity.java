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

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.githubrepo.models.Repository;
import com.example.githubrepo.services.event.BusEvent;
import com.example.githubrepo.services.event.LoadReposEvent;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;
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

    @BindView(R.id.rv_results)
    RecyclerView rvResults;
    @BindView(R.id.et_query)
    EditText etQuery;
    @BindView(R.id.pb_loading)
    ProgressBar pbLoading;
    @BindView(R.id.btn_back)
    ImageButton btnBack;
    @BindView(R.id.tv_no_results)
    TextView tvNoResults;
    @BindView(R.id.btn_search)
    ImageButton btnSearch;
    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;

    @BindString(R.string.no_search_history_msg) String mNoHistMsg;

    private RepoListAdapter mAdapter;
    private List<Repository> mRepoList;
    private GridLayoutManager mLayoutManager;
    private boolean mIsLoading = false;
    private boolean mIsAllLoaded = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initToolbar();
        initEditText();

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
        rvResults.setLayoutManager(mLayoutManager);
        rvResults.setAdapter(mAdapter = new RepoListAdapter(this, mRepoList));

        if (mRepoList.size() == 0 && !mIsLoading) {
            // Check last search
            String lastSearch = mSharedPref.getString(Constants.SP_LAST_QUERY, "");
            if (lastSearch.equals("")) {
                // On first load, generate popular repositories
                getRepoList(QUERY_ON_FIRST_LOAD, SORT_BY_UPDATED, 1);
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, mNoHistMsg, Snackbar.LENGTH_LONG);

                snackbar.show();
            } else {
                getRepoList(lastSearch, SORT_BY_STARS, 1);
                etQuery.setText(lastSearch);
                etQuery.setSelection(etQuery.getText().length());
            }
        }

        etQuery.setCompoundDrawables(null, null, etQuery.getText().toString().equals("") ? null : x, null);
    }


    private void initEditText() {
        etQuery.requestFocus();
        x = getResources().getDrawable(R.drawable.ic_clear_white_24dp);

        x.setBounds(0, 0, x.getIntrinsicWidth(), x.getIntrinsicHeight());
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

    private void initToolbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    private void toggleShowSearch(boolean isShow) {
        if (isShow) {
            btnSearch.setVisibility(View.GONE);
            etQuery.setVisibility(View.VISIBLE);
            btnBack.setVisibility(View.VISIBLE);
            etQuery.requestFocus();
        } else {
            etQuery.setVisibility(View.GONE);
            btnBack.setVisibility(View.GONE);
            btnSearch.setVisibility(View.VISIBLE);
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(KEY_REPO_LIST, (ArrayList<? extends Parcelable>) mRepoList);
        outState.putBoolean(KEY_IS_LOADING, mIsLoading);
        outState.putBoolean(KEY_IS_ALL_LOADED, mIsAllLoaded);
        outState.putBoolean(KEY_IS_SHOW_SEARCH, btnSearch.getVisibility() == View.GONE);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Add listeners
        etQuery.addTextChangedListener(mTextWatcher);
        etQuery.setOnEditorActionListener(mEditorActionListener);
        rvResults.addOnScrollListener(mOnScrollListener);

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
                if (tvNoResults.getVisibility() == View.GONE)
                    tvNoResults.setVisibility(View.VISIBLE);
            }

        } else {
            if (tvNoResults.getVisibility() == View.VISIBLE)
                tvNoResults.setVisibility(View.GONE);
            int totalCount = event.getData().get(0).getTotal();
            int posStart = mRepoList.size();
            mRepoList.addAll(event.getData());
            mAdapter.notifyItemRangeInserted(posStart, event.getData().size());
            mIsLoading = false;
            if (mRepoList.size() == totalCount)
                mIsAllLoaded = true;
        }
    }

    @OnClick(R.id.btn_back)
    public void clickBack() {
        toggleShowSearch(false);
    }

    @OnClick(R.id.btn_search)
    public void clickSearch() {
        toggleShowSearch(true);
    }

    private void getRepoList(String query, String sort, int pageNum) {
        showProgressBar();
        mIsLoading = true;

        Call<List<Repository>> mCallProductList = mService.listRepos(query, sort, null, pageNum,
                Constants.NUM_LOADED);


        mCallProductList.enqueue(new Callback<List<Repository>>() {
            @Override
            public void onResponse(Call<List<Repository>> call, Response<List<Repository>> response) {
                LoadReposEvent event = (LoadReposEvent) getEvent(
                        BusEvent.EventType.REPOS);
                event.setData(response.body());
                post(event);
            }

            @Override
            public void onFailure(Call<List<Repository>> call, Throwable t) {
                hideProgressBar();
                Log.d(TAG, t.getMessage());
                t.printStackTrace();
                mRepoList.clear();
                mAdapter.notifyDataSetChanged();
                tvNoResults.setVisibility(View.VISIBLE);
            }
        });
    }

    private TextWatcher mTextWatcher =  new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            etQuery.setCompoundDrawables(
                    null,
                    null,
                    etQuery.getText().toString().equals("") ? null : x, null);

        }
    };

    private TextView.OnEditorActionListener mEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                mRepoList.clear();
                mIsAllLoaded = false;
                getRepoList(etQuery.getText().toString(), SORT_BY_STARS, 1);
                hideSoftKeyboard();
                return true;
            }
            return false;
        }
    };

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            int totalItemCount = mLayoutManager.getItemCount();
            int lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();

            if (dy != 0 && !mIsLoading && totalItemCount <= (lastVisibleItem + VISIBLE_THRESHOLD)) {
                onLoadMore();
            }
        }
    };

    private void onLoadMore() {
        if (!mIsAllLoaded && !etQuery.getText().toString().equals("")) {
            // Load data
            int index = mRepoList.size();
            getRepoList(etQuery.getText().toString(), SORT_BY_STARS, index / Constants.NUM_LOADED + 1);
        }
    }

    private void showProgressBar() {
        pbLoading.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        pbLoading.setVisibility(View.GONE);
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
        etQuery.removeTextChangedListener(mTextWatcher);
        etQuery.setOnEditorActionListener(null);
        rvResults.removeOnScrollListener(mOnScrollListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LoadReposEvent event = (LoadReposEvent) getEvent(
                BusEvent.EventType.REPOS);
        event.setData(mRepoList);

        mSharedPref.edit().putString(Constants.SP_LAST_QUERY, etQuery.getText().toString()).apply();
    }

}
