package com.example.githubrepo

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.text.SpannableString
import android.text.format.DateUtils
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.githubrepo.databinding.ListItemRepoBinding
import com.example.githubrepo.models.Repository
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class RepoListAdapter(
        private val activity: AppCompatActivity,
        private val repoList: List<Repository>
) : RecyclerView.Adapter<RepoListAdapter.ViewHolder>() {
    private val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ListItemRepoBinding = ListItemRepoBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = repoList[position]
        val fullName = item.fullName.orEmpty()
        val spanString = if (fullName.isEmpty()) {
            SpannableString("")
        } else {
            SpannableString(fullName).also {
                it.setSpan(StyleSpan(Typeface.BOLD), fullName.indexOf("/") + 1, it.length, 0)
                it.setSpan(StyleSpan(Typeface.ITALIC), fullName.indexOf("/") + 1, it.length, 0)
            }
        }
        with(holder.binding) {
            tvFullName.text = spanString
            tvDesc.text = item.desc
            tvLanguage.text = item.language
            tvStars.text = item.stargazersCount.toString()
            tvForks.text = item.forksCount.toString()
        }
        item.pushedAt?.let {
            holder.binding.tvLastUpdated.text = DateUtils.getRelativeTimeSpanString(
                    convertStringToTimeMillis(it),
                    System.currentTimeMillis(),
                    DateUtils.DAY_IN_MILLIS)
        }
        Glide.with(activity)
                .load(item.ownerAvatarUrl)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(holder.binding.civProfile)
    }

    override fun getItemCount(): Int {
        return repoList.size
    }

    inner class ViewHolder(val binding: ListItemRepoBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            this.binding.root.setOnClickListener {
                val url = repoList[adapterPosition].htmlUrl
                val intent = Intent(Intent.ACTION_VIEW).also { it.data = Uri.parse(url) }
                try {
                    activity.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(activity, "No browser app available", LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun convertStringToTimeMillis(dateString: String): Long {
        var milliseconds: Long = 0
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
        try {
            val date = sdf.parse(dateString.replace("Z$".toRegex(), "+0000"))
            milliseconds = date?.time ?: 0
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return milliseconds
    }
}