package com.android.vaibhav.imagessearch;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

/**
 * Created by Vaibhav on 11/21/2015.
 */
public class HttpImageDownloader {

    public static ImageLoader getImageLoader() {
        return ImageLoader.getInstance();
    }

    public static DisplayImageOptions options = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .resetViewBeforeLoading(true)
            .displayer(new FadeInBitmapDisplayer(500))
            .showImageForEmptyUri(R.drawable.empty)
            .showImageOnFail(R.drawable.empty)
            .build();
}
