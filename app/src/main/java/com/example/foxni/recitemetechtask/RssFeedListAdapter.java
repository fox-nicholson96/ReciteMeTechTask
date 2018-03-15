package com.example.foxni.recitemetechtask;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by foxni on 3/15/2018.
 */

public class RssFeedListAdapter
        extends RecyclerView.Adapter<RssFeedListAdapter.FeedModelViewHolder> {

    private final List<RssFeedModel> mRssFeedModels;

    public static class FeedModelViewHolder extends RecyclerView.ViewHolder {
        private final View rssFeedView;

        public FeedModelViewHolder(View v) {
            super(v);
            rssFeedView = v;
        }
    }

    public RssFeedListAdapter(List<RssFeedModel> rssFeedModels) {
        mRssFeedModels = rssFeedModels;
    }

    @NonNull
    @Override
    public FeedModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rss_feed_list_item, parent, false);
        return new FeedModelViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedModelViewHolder holder, int position) {
        final RssFeedModel rssFeedModel = mRssFeedModels.get(position);
        findKeyWords(rssFeedModel.title, (TextView)holder.rssFeedView.findViewById(R.id.titleText));
        findKeyWords(rssFeedModel.description, (TextView)holder.rssFeedView.findViewById(R.id.descriptionText));
    }

    @Override
    public int getItemCount() {
        return mRssFeedModels.size();
    }

    private void findKeyWords(String text, TextView textView) {
        String[] alpha = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m"};
        String[] words = text.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].replaceAll("^\"|\"$", "");
        }

        ArrayList<String> wordsToChange = new ArrayList<>();

        for (String word : words) {
            for (String anAlpha : alpha) {
                if (word.startsWith(anAlpha)) {
                    wordsToChange.add(word);
                }
            }
        }

        for (String word : wordsToChange) {
            text = text.replaceAll("\\b"+word+"\\b","<span style='background-color: #FFFF00'>"+word+"</span>");
        }

        textView.setText(Html.fromHtml(text));
    }
}
