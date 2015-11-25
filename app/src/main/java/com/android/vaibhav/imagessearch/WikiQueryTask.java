package com.android.vaibhav.imagessearch;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by Vaibhav on 11/21/2015.
 */
public class WikiQueryTask extends AsyncTask<String, Void, Integer> {

    private static final String TAG = WikiQueryTask.class.getSimpleName();

    public static final int SEARCH_QUERY_RESULT_OK = 10;
    public static final int SEARCH_QUERY_RESULT_FAIL = -10;
    public static final int SEARCH_QUERY_IO_EXCEPTION = -11;

    List<SearchResult> list;        // list of search results
    Context mContext;               //MainActivity context
    Handler mHandler;               // UI Handler

    WikiQueryTask(Context context, List<SearchResult> list, Handler handler) {
        mContext = context;
        this.list = list;
        mHandler = handler;
    }

    @Override
    protected Integer doInBackground(String... strings) {
        String address = strings[0];
        int ret = SEARCH_QUERY_RESULT_FAIL;

        try {
            //set up HTTP connection
            URL url = new URL(address);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            //check response code
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return ret;
            }

            //Set input stream and read string output
            InputStream in = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            StringBuilder sb = new StringBuilder("");
            String line = "";

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            in.close();

            String result = sb.toString();

            //parse result for JSON data

            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject jsonQueryObject = jsonObject.optJSONObject("query");
                if (jsonQueryObject == null) return ret;
                JSONObject jsonPageObject = jsonQueryObject.optJSONObject("pages");
                if (jsonPageObject == null) return ret;
                JSONArray jsonPagesArray = jsonPageObject.names();

                int length = jsonPagesArray.length();

                for (int i = 0; i < length; i++) {
                    String id = jsonPagesArray.optString(i);
                    JSONObject page = jsonPageObject.optJSONObject(id);
                    String title = page.optString("title");

                    String imagePath = "";
                    JSONObject thumb = page.optJSONObject("thumbnail");

                    if (thumb != null) {
                        imagePath = thumb.optString("source");
                    }

                    SearchResult searchResult = new SearchResult(id, title, imagePath);
                    list.add(searchResult);
                }
                if (isCancelled()) {
                    Log.d("vaibhav", "task isCancelled Wikiqery task");
                    ret = SEARCH_QUERY_RESULT_FAIL;
                } else {
                    ret = SEARCH_QUERY_RESULT_OK;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                ret = SEARCH_QUERY_IO_EXCEPTION;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            ret = SEARCH_QUERY_IO_EXCEPTION;
        } catch (IOException e) {
            e.printStackTrace();
            ret = SEARCH_QUERY_IO_EXCEPTION;
        }
        return ret;
    }

    @Override
    protected void onPostExecute(Integer what) {
        super.onPostExecute(what);
        Log.d(TAG, "onPostExecute - what : " + what);
        mHandler.sendEmptyMessage(what);
    }
}
