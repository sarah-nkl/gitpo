package com.example.githubrepo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.githubrepo.services.event.LoadReposEvent;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dagger.android.AndroidInjection;
import kotlin.collections.CollectionsKt;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.githubrepo.ConstantsKt.NUM_LOADED;
import static com.example.githubrepo.ConstantsKt.SP_IS_FIRST_LAUNCH;
import static com.example.githubrepo.ConstantsKt.SP_LAST_QUERY;

/**
 * Created by sarahneo on 20/2/17.
 */

public class MainActivity extends BaseSearchActivity {

    private static final String KEY_REPO_LIST = "key_repo_list";
    private static final String KEY_IS_LOADING = "key_is_loading";
    private static final String KEY_IS_SHOW_SEARCH = "key_is_show_search";
    private static final String KEY_IS_ALL_LOADED = "key_is_all_loaded";
    private static final String SORT_BY_STARS = "stars";
    private static final String SORT_BY_UPDATED = "updated";
//    private static final String SORT_BY_FORKS = "forks";
    private static final String QUERY_ON_FIRST_LOAD = "stars:>5000";
    private static final int VISIBLE_THRESHOLD = 5;

    private RecyclerView rvResults;
    private EditText etQuery;
    private ProgressBar pbLoading;
    private ImageButton btnBack;
    private TextView tvNoResults;
    private ImageButton btnSearch;
    private CoordinatorLayout coordinatorLayout;

    private String mNoHistMsg;

    private RepoListAdapter mAdapter;
    private List<Repository> mRepoList;
    private GridLayoutManager mLayoutManager;
    private boolean mIsLoading = false;
    private boolean mIsAllLoaded = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        initViews();
        initToolbar();
        initEditText();

        mRepoList = new ArrayList<>();

        if (savedInstanceState != null) {
            mRepoList = savedInstanceState.getParcelableArrayList(KEY_REPO_LIST);
            mIsLoading = savedInstanceState.getBoolean(KEY_IS_LOADING);
            mIsAllLoaded = savedInstanceState.getBoolean(KEY_IS_ALL_LOADED);
            toggleShowSearch(savedInstanceState.getBoolean(KEY_IS_SHOW_SEARCH));
        }

        mLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.grid_item_list_count));
        rvResults.setLayoutManager(mLayoutManager);
        rvResults.setAdapter(mAdapter = new RepoListAdapter(this, mRepoList));

        if (sharedPref.getBoolean(SP_IS_FIRST_LAUNCH, true) && mRepoList.size() == 0 && !mIsLoading) {
            // Check last search
            String lastSearch = sharedPref.getString(SP_LAST_QUERY, "");
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

            // Set to not first launch
            sharedPref.edit().putBoolean(SP_IS_FIRST_LAUNCH, false).apply();
        }

        etQuery.setCompoundDrawables(null, null, etQuery.getText().toString().equals("") ? null : x, null);
    }


    @SuppressLint("ClickableViewAccessibility")
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

    private void initViews() {
        rvResults = findViewById(R.id.rv_results);
        etQuery = findViewById(R.id.et_query);
        pbLoading = findViewById(R.id.pb_loading);
        btnBack = findViewById(R.id.btn_back);
        btnSearch = findViewById(R.id.btn_search);
        tvNoResults = findViewById(R.id.tv_no_results);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        mNoHistMsg = getString(R.string.no_search_history_msg);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleShowSearch(false);
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleShowSearch(true);
            }
        });
    }

    private void initToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
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

        if (mIsLoading && event.getData().size() != 0)
            populateAndShowResults();

        if (mRepoList.size() == 0 && !mIsLoading)
            tvNoResults.setVisibility(View.VISIBLE);
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
            populateAndShowResults();
            mIsLoading = false;
            if (mRepoList.size() == totalCount)
                mIsAllLoaded = true;
        }
    }

    private void populateAndShowResults() {
        int posStart = mRepoList.size();
        mRepoList.addAll(event.getData());
        mAdapter.notifyItemRangeInserted(posStart, mRepoList.size());
        mIsLoading = false;

        // Clear event data
        event.setData(CollectionsKt.<Repository>emptyList());
    }

    private void getRepoList(String query, String sort, int pageNum) {
        showProgressBar();
        mIsLoading = true;

        Call<List<Repository>> mCallProductList = gitHubService.listRepos(query, sort, null, pageNum,
                NUM_LOADED);

        mCallProductList.enqueue(new Callback<List<Repository>>() {
            @Override
            public void onResponse(Call<List<Repository>> call, Response<List<Repository>> response) {
                event.setData(response.body());
                post(event);
            }

            @Override
            public void onFailure(Call<List<Repository>> call, Throwable t) {
                hideProgressBar();
                t.printStackTrace();
                mIsAllLoaded = true;
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
            getRepoList(etQuery.getText().toString(), SORT_BY_STARS, index / NUM_LOADED + 1);
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
    public void onBackPressed() {
        super.onBackPressed();
        sharedPref.edit().putString(SP_LAST_QUERY, etQuery.getText().toString()).apply();
        sharedPref.edit().putBoolean(SP_IS_FIRST_LAUNCH, true).apply();
    }

}
