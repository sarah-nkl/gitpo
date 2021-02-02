package com.example.githubrepo

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.githubrepo.databinding.ActivityMainBinding
import com.example.githubrepo.models.RepositoryList
import com.example.githubrepo.services.BusProvider
import com.example.githubrepo.services.GitHubService
import com.example.githubrepo.services.event.BusEvent
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

private const val GITHUB_SEARCH_URL = "https://api.github.com/"

abstract class BaseSearchActivity : AppCompatActivity() {

    protected lateinit var service: GitHubService
    protected lateinit var sharedPref: SharedPreferences
    protected lateinit var binding: ActivityMainBinding
    @Inject internal lateinit var busProvider: BusProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)
        (application as MyApplication).inject(this)
        sharedPref = getPreferences(Context.MODE_PRIVATE)
        initGitHubService()
    }

    private fun initGitHubService() {
        val builder = GsonBuilder()
        builder.registerTypeAdapter(RepositoryList::class.java, RepoListDeserializer())
        val retrofit = Retrofit.Builder()
                .baseUrl(GITHUB_SEARCH_URL)
                .addConverterFactory(GsonConverterFactory.create(builder.create()))
                .build()
        service = retrofit.create(GitHubService::class.java)
    }

    override fun onStart() {
        super.onStart()
        try {
            busProvider.register(this)
        } catch (e: IllegalArgumentException) {
        }
    }

    override fun onStop() {
        super.onStop()
        busProvider.unregister(this)
    }

    fun post(event: Any?) {
        if (event != null) {
            busProvider.post(event)
        }
    }

    fun getEvent(type: BusEvent.EventType): Any? {
        return busProvider.getEvent(type)
    }
}