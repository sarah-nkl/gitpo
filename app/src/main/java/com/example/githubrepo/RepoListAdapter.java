package com.example.githubrepo;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.githubrepo.models.Repository;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by sarahneo on 20/2/17.
 */

public class RepoListAdapter extends RecyclerView.Adapter<RepoListAdapter.ViewHolder> {

    private AppCompatActivity mActivity;
    private List<Repository> mRepoList;

    public RepoListAdapter(AppCompatActivity activity, List<Repository> repoList) {
        this.mActivity = activity;
        this.mRepoList = repoList;
    }

    @Override
    public RepoListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_repo, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final RepoListAdapter.ViewHolder holder, int position) {

        final Repository item = mRepoList.get(position);

        String fullName = item.getFullName();
        SpannableString spanString = new SpannableString(fullName);
        spanString.setSpan(new StyleSpan(Typeface.BOLD), fullName.indexOf("/") + 1, spanString.length(), 0);
        spanString.setSpan(new StyleSpan(Typeface.ITALIC), fullName.indexOf("/") + 1, spanString.length(), 0);
        holder.tvFullName.setText(spanString);

        holder.tvDesc.setText(item.getDesc());
        holder.tvLanguage.setText(item.getLanguage());
        holder.tvStars.setText(String.format("%s", item.getStargazersCount()));
        holder.tvForks.setText(String.format("%s", item.getForksCount()));
        if (item.getPushedAt() != 0) {
            holder.tvLastUpdated.setText(DateUtils.getRelativeTimeSpanString(
                    item.getPushedAt(),
                    System.currentTimeMillis(),
                    DateUtils.DAY_IN_MILLIS));
        }

        Glide.with(holder.civProfile.getContext())
                .load(item.getOwnerAvatarUrl())
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(holder.civProfile);
    }

    @Override
    public int getItemCount() {
        return mRepoList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName;
        TextView tvDesc;
        TextView tvLanguage;
        TextView tvStars;
        TextView tvForks;
        TextView tvLastUpdated;
        CircleImageView civProfile;
        RelativeLayout rlRepoContainer;

        ViewHolder(View itemView) {
            super(itemView);

            tvFullName = itemView.findViewById(R.id.tv_full_name);
            tvDesc = itemView.findViewById(R.id.tv_desc);
            tvLanguage = itemView.findViewById(R.id.tv_language);
            tvStars = itemView.findViewById(R.id.tv_stars);
            tvForks = itemView.findViewById(R.id.tv_forks);
            tvLastUpdated = itemView.findViewById(R.id.tv_last_updated);
            civProfile = itemView.findViewById(R.id.civ_profile);
            rlRepoContainer = itemView.findViewById(R.id.rl_repo_container);

            rlRepoContainer.setOnClickListener(new View.OnClickListener() {
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
}
