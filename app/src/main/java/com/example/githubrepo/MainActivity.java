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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
    private static final String KEY_TOTAL_RESULTS = "key_total_results";
    private static final String SORT_BY_STARS = "stars";
    private static final String SORT_BY_UPDATED = "updated";
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

    private RepoListAdapter mAdapter;
    private List<Repository> mRepoList;
    private LinearLayoutManager mLayoutManager;
    private boolean mIsShowSearch = false;
    private boolean mIsLoading = false;
    private boolean isAllLoaded = false;
    private boolean isExecuted = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        if (mRepoList.size() == 0) {
            // Check last search
            String lastSearch = mSharedPref.getString(Constants.SP_LAST_QUERY, "");
            if (lastSearch.equals(""))
                // On first load, generate popular repositories
                getRepoList("stars:>5000", SORT_BY_UPDATED, 1);
            else {
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

    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(KEY_REPO_LIST, (ArrayList<? extends Parcelable>) mRepoList);
        outState.putBoolean(KEY_IS_LOADING, mIsLoading);
        outState.putBoolean(KEY_IS_SHOW_SEARCH, !menuItemSearch.isVisible());
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
    protected void onPause() {
        super.onPause();
        hideProgressBar();
    }

    @Subscribe
    public void OnLoadReposEvent(LoadReposEvent event) {
        hideProgressBar();

        if (event.getData() == null || event.getData().size() == 0) {
            isAllLoaded = true;
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
                isAllLoaded = true;
        }
    }

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

    private void getRepoList(String query, String sort, int pageNum) {
        showProgressBar();

        Call<List<Repository>> productList = mService.listRepos(query, sort, null, pageNum,
                Constants.NUM_LOADED);

        if (isExecuted) {
            isExecuted = false;
            productList.enqueue(new Callback<List<Repository>>() {
                @Override
                public void onResponse(Call<List<Repository>> call, Response<List<Repository>> response) {
                    LoadReposEvent event = (LoadReposEvent) getEvent(
                            BusEvent.EventType.REPOS);
                    event.setData(response.body());
                    post(event);
                    isExecuted = true;
                }

                @Override
                public void onFailure(Call<List<Repository>> call, Throwable t) {
                    hideProgressBar();
                    Log.d(TAG, t.getMessage());
                    t.printStackTrace();
                    mRepoList.clear();
                    mAdapter.notifyDataSetChanged();
                    tvNoResults.setVisibility(View.VISIBLE);
                    isExecuted = true;
                }
            });
        }
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
                isAllLoaded = false;
                getRepoList(etQuery.getText().toString(), SORT_BY_UPDATED, 1);
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

            if (!mIsLoading && totalItemCount <= (lastVisibleItem + VISIBLE_THRESHOLD)) {
                onLoadMore();
                mIsLoading = true;
            }
        }
    };

    private void onLoadMore() {
        if (!isAllLoaded && !etQuery.getText().toString().equals("")) {
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
