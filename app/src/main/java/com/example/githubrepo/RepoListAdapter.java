package com.example.githubrepo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by sarahneo on 20/2/17.
 */

public class RepoListAdapter extends RecyclerView.Adapter<RepoListAdapter.ViewHolder> {

    private LayoutInflater inflater = null;
    private AppCompatActivity mActivity;
    private List<Repository> mRepoList;

    public RepoListAdapter(AppCompatActivity activity, List<Repository> repoList) {
        this.mActivity = activity;
        this.mRepoList = repoList;
        inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public RepoListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.list_item_repo, parent, false);

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
        holder.tvStars.setText(Integer.toString(item.getStargazersCount()));
        holder.tvForks.setText(Integer.toString(item.getForksCount()));
        if (item.getPushedAt() != null) {
            holder.tvLastUpdated.setText(DateUtils.getRelativeTimeSpanString(
                    convertStringToTimeMillis(item.getPushedAt()),
                    System.currentTimeMillis(),
                    DateUtils.DAY_IN_MILLIS));
        }

        Glide.with(mActivity)
                .load(item.getOwnerAvatarUrl())
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(holder.civProfile);
    }

    @Override
    public int getItemCount() {
        return mRepoList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_full_name) TextView tvFullName;
        @BindView(R.id.tv_desc) TextView tvDesc;
        @BindView(R.id.tv_language) TextView tvLanguage;
        @BindView(R.id.tv_stars) TextView tvStars;
        @BindView(R.id.tv_forks) TextView tvForks;
        @BindView(R.id.tv_last_updated) TextView tvLastUpdated;
        @BindView(R.id.civ_profile) CircleImageView civProfile;
        @BindView(R.id.rl_repo_container) RelativeLayout rlRepoContainer;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.rl_repo_container)
        public void clickItem() {
            Repository repo = mRepoList.get(getAdapterPosition());
            String url = repo.getHtmlUrl();
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            if (i.resolveActivity(mActivity.getPackageManager()) != null)
                mActivity.startActivity(i);
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
