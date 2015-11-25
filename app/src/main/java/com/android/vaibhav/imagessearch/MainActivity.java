package com.android.vaibhav.imagessearch;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final int HANDLER_START_SEARCH = 1;
    public static final int HANDLER_STOP_SEARCH = 2;
    public static final String query = "https://en.wikipedia.org/w/api.php?action=query&prop=pageimages&format=json&piprop=thumbnail&pithumbsize=400&pilimit=50&generator=prefixsearch&gpslimit=50&gpssearch=";

    private EditText editText;
    private GridView gridView;
    private TextView textView;

    private boolean isCancelled = false;

    private WikiQueryTask task = null;
    private SearchResultsAdapter adapter = null;
    private List<SearchResult> searchResults;
    private Context mContext;

    Handler textSearchHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Log.d(TAG, "handleMessage - msg.what : "  + msg.what);

            switch (msg.what) {
                case HANDLER_START_SEARCH:
                    removeMessages(HANDLER_START_SEARCH);
                    downloadJSONData();
                    break;
                case HANDLER_STOP_SEARCH:
                    if (task != null) {
                        task.cancel(true);
                        isCancelled = true;
                    }
                    gridView.setVisibility(View.GONE);
                    textView.setVisibility(View.VISIBLE);
                    if (editText.getText().length() == 0) {
                        textView.setText("Please enter a search keyword");
                    } else {
                        textView.setText("No results found");
                    }
                    break;
                case WikiQueryTask.SEARCH_QUERY_RESULT_OK:
                    downloadShowImages();
                    break;
                case WikiQueryTask.SEARCH_QUERY_IO_EXCEPTION:
                    textSearchHandler.removeMessages(HANDLER_START_SEARCH);
                    gridView.setVisibility(View.GONE);
                    textView.setVisibility(View.VISIBLE);
                    if (editText.getText().length() == 0) {
                        textView.setText("Please enter a search keyword");
                    } else {
                        textView.setText("Network error");
                    }
                    break;
                case WikiQueryTask.SEARCH_QUERY_RESULT_FAIL:
                    textSearchHandler.removeMessages(HANDLER_START_SEARCH);
                    gridView.setVisibility(View.GONE);
                    textView.setVisibility(View.VISIBLE);
                    if (editText.getText().length() == 0) {
                        textView.setText("Please enter a search keyword");
                    } else {
                        textView.setText("No results found");
                    }
                    break;
                default:
                    break;

            }
        }
    };
    TextWatcher searchTextChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (charSequence.length() == 0) {
                textView.setVisibility(View.VISIBLE);
                textView.setText("Please enter a search keyword");
                gridView.setVisibility(View.GONE);
                textSearchHandler.removeMessages(HANDLER_START_SEARCH);
                textSearchHandler.sendEmptyMessage(HANDLER_STOP_SEARCH);
                return;
            }
            isCancelled = false;
            textSearchHandler.removeMessages(HANDLER_START_SEARCH);
            textSearchHandler.sendEmptyMessageDelayed(HANDLER_START_SEARCH, 200);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    public static boolean getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return true;

            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheSize(25 * 1024 * 1024)
                .threadPriority(Thread.NORM_PRIORITY)
                .memoryCacheSize(5 * 1024 * 1024)
                .build();
        ImageLoader.getInstance().init(config);

        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.editText);
        gridView = (GridView) findViewById(R.id.searchResultsGrid);
        textView = (TextView) findViewById(R.id.noResultsText);

        textView.setText("Please enter a search keyword");

        editText.addTextChangedListener(searchTextChangeListener);
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int state) {
                Log.d(TAG, "onScrollStateChanged - state : " + state);
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void downloadShowImages() {
        if (!isCancelled) {
            gridView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);

            if (adapter == null) {
                adapter = new SearchResultsAdapter(MainActivity.this, HttpImageDownloader.getImageLoader(), searchResults);
                gridView.setAdapter(adapter);
            } else {
                adapter.updateResults(searchResults);
            }
        }
    }

    public void downloadJSONData() {
        Editable edit = editText.getText();

        if (edit == null || edit.toString().isEmpty()) {
            return;
        }
        if (searchResults != null) {
            searchResults.clear();
        }
        searchResults = new ArrayList<>(50);
        if (task != null) {
            task.cancel(true);
            task = null;
        }

        if (getConnectivityStatus(mContext)) {
            textView.setText("Searching...");
        } else {
            textView.setText("Network error");
            return;
        }
        task = new WikiQueryTask(this, searchResults, textSearchHandler);
        String address = query + edit.toString();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, address);
    }
}