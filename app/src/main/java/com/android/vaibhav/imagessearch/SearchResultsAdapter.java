package com.android.vaibhav.imagessearch;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.List;

/**
 * Created by Vaibhav on 11/21/2015.
 */
public class SearchResultsAdapter extends BaseAdapter {

    private static final String TAG = SearchResultsAdapter.class.getSimpleName();
    private Context mContext;
    private List<SearchResult> results;
    private final ImageLoader imageLoader;

    SearchResultsAdapter(Context context, ImageLoader loader, List<SearchResult> results) {
        mContext = context;
        imageLoader = loader;
        this.results = results;
    }

    public void updateResults(List<SearchResult> newResults) {
        this.results = newResults;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return results.size();
    }

    @Override
    public Object getItem(int i) {
        return results.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {

        ResultHolder holder = null;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.result_item, parent, false);
            holder = new ResultHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            holder.loadProgress = (ProgressBar) convertView.findViewById(R.id.loadingProgress);
            convertView.setTag(holder);
        } else {
            holder = (ResultHolder) convertView.getTag();
        }

        final ResultHolder finalHolder = holder;
        String imageURL = results.get(i).getSource();

        imageLoader.displayImage(imageURL, finalHolder.imageView, HttpImageDownloader.options, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {
                finalHolder.loadProgress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                Log.d(TAG, "onLoadingFailed : Image : " + s);
                Log.d(TAG, "onLoadingFailed : Fail reason : " + failReason.getType());
                finalHolder.loadProgress.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                finalHolder.loadProgress.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
                finalHolder.loadProgress.setVisibility(View.GONE);
            }
        });
        return convertView;
    }

    static class ResultHolder {
        ImageView imageView;
        ProgressBar loadProgress;
    }
}