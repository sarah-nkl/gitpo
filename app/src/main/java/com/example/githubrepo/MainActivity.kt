package com.example.githubrepo

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView.OnEditorActionListener
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.githubrepo.models.Repository
import com.example.githubrepo.models.RepositoryList
import com.example.githubrepo.services.event.BusEvent
import com.example.githubrepo.services.event.LoadReposEvent
import com.google.android.material.snackbar.Snackbar
import com.squareup.otto.Subscribe
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class MainActivity : BaseSearchActivity() {

    private val noHistMsg: String by lazy {
        resources.getString(R.string.no_search_history_msg)
    }
    private var adapter: RepoListAdapter? = null
    private var repoList: ArrayList<Repository>? = ArrayList()
    private lateinit var layoutManager: GridLayoutManager
    private var isLoading = false
    private var isAllLoaded = false
    private var x: Drawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initToolbar()
        initEditText()
        initButtons()
        val event = getEvent(BusEvent.EventType.REPOS) as LoadReposEvent?
        if (savedInstanceState != null) {
            repoList = savedInstanceState.getParcelableArrayList(KEY_REPO_LIST)
            isLoading = savedInstanceState.getBoolean(KEY_IS_LOADING)
            isAllLoaded = savedInstanceState.getBoolean(KEY_IS_ALL_LOADED)
            toggleShowSearch(savedInstanceState.getBoolean(KEY_IS_SHOW_SEARCH))
        } else if (event?.data?.isNotEmpty() == true) {
            repoList?.addAll(event.data!!)
        }
        layoutManager = GridLayoutManager(this, resources.getInteger(R.integer.grid_item_list_count))
        binding.rvResults.layoutManager = layoutManager
        binding.rvResults.adapter = RepoListAdapter(this, repoList.orEmpty()).also { adapter = it }
        if (repoList?.size == 0 && !isLoading) {
            // Check last search
            val lastSearch = sharedPref.getString(SP_LAST_QUERY, "")
            if (lastSearch == "") {
                // On first load, generate popular repositories
                getRepoList(QUERY_ON_FIRST_LOAD, SORT_BY_UPDATED, 1)
                Snackbar.make(binding.coordinatorLayout, noHistMsg, Snackbar.LENGTH_LONG).show()
            } else {
                getRepoList(lastSearch, SORT_BY_STARS, 1)
                binding.etQuery.setText(lastSearch)
                binding.etQuery.setSelection(binding.etQuery.text.length)
            }
        }
        binding.etQuery.setCompoundDrawables(null, null, if (binding.etQuery.text.toString() == "") null else x, null)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initEditText() {
        binding.etQuery.requestFocus()
        x = ContextCompat.getDrawable(this, R.drawable.ic_clear_white_24dp)
        x?.let {
            it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
        }
        binding.etQuery.setOnTouchListener(OnTouchListener { _, event ->
            if (binding.etQuery.compoundDrawables[2] == null) {
                return@OnTouchListener false
            }
            if (event.action != MotionEvent.ACTION_UP) {
                return@OnTouchListener false
            }
            if (event.x > binding.etQuery.width - binding.etQuery.paddingRight - x!!.intrinsicWidth) {
                binding.etQuery.setText("")
                binding.etQuery.setCompoundDrawables(null, null, null, null)
            }
            false
        })
    }

    private fun initButtons() {
        binding.btnBack.setOnClickListener { toggleShowSearch(false) }
        binding.btnSearch.setOnClickListener { toggleShowSearch(true) }
    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbar)
    }

    private fun toggleShowSearch(isShow: Boolean) {
        if (isShow) {
            binding.btnSearch.isVisible = false
            binding.etQuery.isVisible = true
            binding.btnBack.isVisible = true
            binding.etQuery.requestFocus()
        } else {
            binding.etQuery.isVisible = false
            binding.btnBack.isVisible = false
            binding.btnSearch.isVisible = true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(KEY_REPO_LIST, repoList as ArrayList<out Parcelable?>?)
        outState.putBoolean(KEY_IS_LOADING, isLoading)
        outState.putBoolean(KEY_IS_ALL_LOADED, isAllLoaded)
        outState.putBoolean(KEY_IS_SHOW_SEARCH, !binding.btnSearch.isVisible)
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()

        // Add listeners
        binding.etQuery.addTextChangedListener(mTextWatcher)
        binding.etQuery.setOnEditorActionListener(editorActionListener)
        binding.rvResults.addOnScrollListener(onScrollListener)
    }

    override fun onResume() {
        super.onResume()
        if (isLoading) {
            showProgressBar()
        }
    }

    override fun onPause() {
        super.onPause()
        hideProgressBar()
    }

    @Subscribe
    fun OnLoadReposEvent(event: LoadReposEvent) {
        hideProgressBar()
        isLoading = false
        val data = event.data
        if (data.isNullOrEmpty()) {
            isAllLoaded = true
            if (repoList?.size == 0) {
                // No results found
                if (!binding.tvNoResults.isVisible) binding.tvNoResults.isVisible = true
            }
        } else {
            if (binding.tvNoResults.isVisible) binding.tvNoResults.isVisible = false
            val totalCount = data[0].total
            val posStart = repoList?.size ?: 0
            repoList?.addAll(data)
            adapter?.notifyItemRangeInserted(posStart, data.size)
            if (repoList?.size == totalCount) isAllLoaded = true
        }
    }

    private fun getRepoList(query: String?, sort: String, pageNum: Int) {
        query ?: return
        showProgressBar()
        isLoading = true
        val callProductList = service.listRepos(query, sort, null, pageNum,
                NUM_LOADED)
        callProductList.enqueue(object : Callback<RepositoryList> {
            override fun onResponse(call: Call<RepositoryList>, response: Response<RepositoryList>) {
                val event = getEvent(
                        BusEvent.EventType.REPOS) as LoadReposEvent?
                event?.setData(response.body()?.repos)
                post(event)
                isLoading = false
            }

            override fun onFailure(call: Call<RepositoryList>, t: Throwable) {
                hideProgressBar()
                val message = t.message
                if (message != null) {
                    Log.d(TAG, message)
                }
                t.printStackTrace()
                repoList?.clear()
                adapter?.notifyDataSetChanged()
                binding.tvNoResults.isVisible = true
                isLoading = false
            }
        })
    }

    private val mTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            binding.etQuery.setCompoundDrawables(
                    null,
                    null,
                    if (binding.etQuery.text.toString() == "") null else x, null)
        }
    }
    private val editorActionListener = OnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            repoList?.clear()
            isAllLoaded = false
            getRepoList(binding.etQuery.text.toString(), SORT_BY_STARS, 1)
            hideSoftKeyboard()
            return@OnEditorActionListener true
        }
        false
    }
    private val onScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val totalItemCount = layoutManager.itemCount
            val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
            if (dy != 0 && !isLoading && totalItemCount <= lastVisibleItem + VISIBLE_THRESHOLD) {
                onLoadMore()
            }
        }
    }

    private fun onLoadMore() {
        if (!isAllLoaded && binding.etQuery.text.toString().isNotEmpty()) {
            // Load data
            val index = repoList?.size ?: return
            getRepoList(binding.etQuery.text.toString(), SORT_BY_STARS, index / NUM_LOADED + 1)
        }
    }

    private fun showProgressBar() {
        binding.pbLoading.isVisible = true
    }

    private fun hideProgressBar() {
        binding.pbLoading.isVisible = false
    }

    private fun hideSoftKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus()
        }
    }

    override fun onStop() {
        super.onStop()

        // Remove listeners
        binding.etQuery.removeTextChangedListener(mTextWatcher)
        binding.etQuery.setOnEditorActionListener(null)
        binding.rvResults.removeOnScrollListener(onScrollListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        val event = getEvent(
                BusEvent.EventType.REPOS) as LoadReposEvent?
        event?.setData(repoList)
        sharedPref.edit().putString(SP_LAST_QUERY, binding.etQuery.text.toString()).apply()
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val KEY_REPO_LIST = "key_repo_list"
        private const val KEY_IS_LOADING = "key_is_loading"
        private const val KEY_IS_SHOW_SEARCH = "key_is_show_search"
        private const val KEY_IS_ALL_LOADED = "key_is_all_loaded"
        private const val SORT_BY_STARS = "stars"
        private const val SORT_BY_UPDATED = "updated"

        //    private static final String SORT_BY_FORKS = "forks";
        private const val QUERY_ON_FIRST_LOAD = "stars:>5000"
        private const val VISIBLE_THRESHOLD = 5
        private const val SP_LAST_QUERY = "sp_last_query"
        private const val NUM_LOADED = 20
    }
}