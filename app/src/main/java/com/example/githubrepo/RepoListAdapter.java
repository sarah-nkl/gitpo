package com.example.githubrepo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.githubrepo.databinding.ListItemRepoBinding;
import com.example.githubrepo.models.Repository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by sarahneo on 20/2/17.
 */

public class RepoListAdapter extends RecyclerView.Adapter<RepoListAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    private final AppCompatActivity mActivity;
    private final List<Repository> mRepoList;

    public RepoListAdapter(AppCompatActivity activity, List<Repository> repoList) {
        this.mActivity = activity;
        this.mRepoList = repoList;
        inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public RepoListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemRepoBinding binding = ListItemRepoBinding.inflate(inflater, parent, false);

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final RepoListAdapter.ViewHolder holder, int position) {

        final Repository item = mRepoList.get(position);

        String fullName = item.getFullName();
        SpannableString spanString = new SpannableString(fullName);
        spanString.setSpan(new StyleSpan(Typeface.BOLD), fullName.indexOf("/") + 1, spanString.length(), 0);
        spanString.setSpan(new StyleSpan(Typeface.ITALIC), fullName.indexOf("/") + 1, spanString.length(), 0);
        holder.mBinding.tvFullName.setText(spanString);

        holder.mBinding.tvDesc.setText(item.getDesc());
        holder.mBinding.tvLanguage.setText(item.getLanguage());
        holder.mBinding.tvStars.setText(Integer.toString(item.getStargazersCount()));
        holder.mBinding.tvForks.setText(Integer.toString(item.getForksCount()));
        if (item.getPushedAt() != null) {
            holder.mBinding.tvLastUpdated.setText(DateUtils.getRelativeTimeSpanString(
                    convertStringToTimeMillis(item.getPushedAt()),
                    System.currentTimeMillis(),
                    DateUtils.DAY_IN_MILLIS));
        }

        Glide.with(mActivity)
                .load(item.getOwnerAvatarUrl())
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(holder.mBinding.civProfile);
    }

    @Override
    public int getItemCount() {
        return mRepoList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ListItemRepoBinding mBinding;

        ViewHolder(ListItemRepoBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            mBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Repository repo = mRepoList.get(getAdapterPosition());
                    String url = repo.getHtmlUrl();
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    if (i.resolveActivity(mActivity.getPackageManager()) != null)
                        mActivity.startActivity(i);
                }
            });
        }
    }

    private static long convertStringToTimeMillis(String dateString) {
        long milliseconds = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        try {
            Date d = sdf.parse(dateString.replaceAll("Z$", "+0000"));
            milliseconds = d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return milliseconds;
    }
}
