package com.example.foxni.recitemetechtask;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeLayout;
    private TextView mFeedTitleTextView;

    private List<RssFeedModel> mFeedModelList;
    private String mFeedTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.recyclerView);
        mSwipeLayout = findViewById(R.id.swipeRefreshLayout);
        mFeedTitleTextView = findViewById(R.id.feedTitle);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        new FetchFeedTask().execute((Void) null);

        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new FetchFeedTask().execute((Void) null);
            }
        });

        final String[] messages = new String[]{"Message 1", "Message 2", "Message 3"};

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Random rn = new Random();
                        int r = rn.nextInt(3);
                        Toast toast = Toast.makeText(MainActivity.this, messages[r], Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }, 60000);
    }

    private class FetchFeedTask extends AsyncTask<Void, Void, Boolean> {

        private String urlLink;
        private final String TAG = "Test RSS Application";

        @Override
        protected void onPreExecute() {
            mSwipeLayout.setRefreshing(true);
            urlLink = "http://feeds.bbci.co.uk/news/technology/rss.xml?edition=uk";
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (TextUtils.isEmpty(urlLink))
                return false;

            try {
                if(!urlLink.startsWith("http://") && !urlLink.startsWith("https://"))
                    urlLink = "http://" + urlLink;

                URL url = new URL(urlLink);
                InputStream inputStream = url.openConnection().getInputStream();
                mFeedModelList = parseFeed(inputStream);
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Error", e);
            } catch (XmlPullParserException e) {
                Log.e(TAG, "Error", e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mSwipeLayout.setRefreshing(false);

            if (success) {
                mFeedTitleTextView.setText(mFeedTitle);
                // Fill RecyclerView
                mRecyclerView.setAdapter(new RssFeedListAdapter(mFeedModelList));
            } else {
                Toast.makeText(MainActivity.this,
                        "Enter a valid Rss feed url",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private List<RssFeedModel> parseFeed(InputStream inputStream) throws XmlPullParserException,
            IOException {
        String title = null;
        String description = null;
        boolean isItem = false;
        List<RssFeedModel> items = new ArrayList<>();

        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlPullParser.setInput(inputStream, null);

            xmlPullParser.nextTag();
            while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                int eventType = xmlPullParser.getEventType();

                String name = xmlPullParser.getName();
                if(name == null)
                    continue;

                if(eventType == XmlPullParser.END_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = false;
                    }
                    continue;
                }

                if (eventType == XmlPullParser.START_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = true;
                        continue;
                    }
                }

                Log.d("MyXmlParser", "Parsing name ==> " + name);
                String result = "";
                if (xmlPullParser.next() == XmlPullParser.TEXT) {
                    result = xmlPullParser.getText();
                    xmlPullParser.nextTag();
                }

                if (name.equalsIgnoreCase("title")) {
                    title = result;
                } else if (name.equalsIgnoreCase("description")) {
                    description = result;
                }

                if (title != null && description != null) {
                    if(isItem) {
                        RssFeedModel item = new RssFeedModel(title, description);
                        items.add(item);
                    }
                    else {
                        mFeedTitle = title;
                    }

                    title = null;
                    description = null;
                    isItem = false;
                }
            }

            return items;
        } finally {
            inputStream.close();
        }
    }
}
