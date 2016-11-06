package com.songhan.viralmix;

import com.daprlabs.aaron.swipedeck.SwipeDeck;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private MainActivity context = this;
    private SwipeDeck cardStack;
    private SwipeDeckAdapter adapter;
    private Button undoButton;
    private int videoIndex = 0;
    private VideoManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = VideoManager.getInstance();
        setContentView(R.layout.activity_main);
        cardStack = (SwipeDeck) findViewById(R.id.swipe_deck);

        cardStack.setCallback(new SwipeDeck.SwipeDeckCallback() {
            @Override
            public void cardSwipedLeft(long stableId) {
                Log.i("MainActivity", "card was swiped left, position in adapter: " + stableId);
                undoButton.setVisibility(View.VISIBLE);
                if (stableId + 3 > manager.videos.size()) {
                    manager.getNextVideos();
                }
                videoIndex++;
            }

            @Override
            public void cardSwipedRight(long stableId) {
                Log.i("MainActivity", "card was swiped right, position in adapter: " + stableId);
                undoButton.setVisibility(View.VISIBLE);
                manager.getNextVideosRelatedTo(manager.videos.get((int)stableId).getId().getVideoId());
                videoIndex++;
            }
        });

        cardStack.setLeftImage(R.id.left_image);
        cardStack.setRightImage(R.id.right_image);

        Button btn = (Button) findViewById(R.id.button_left);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardStack.swipeTopCardLeft(500);
            }
        });
        Button btn2 = (Button) findViewById(R.id.button_right);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardStack.swipeTopCardRight(180);
            }
        });

        undoButton = (Button) findViewById(R.id.button_center);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardStack.unSwipeCard();
                videoIndex--;
                if (videoIndex == 0){
                    undoButton.setVisibility(View.GONE);
                }
            }
        });

        if (savedInstanceState != null){
            videoIndex = savedInstanceState.getInt("page", 0);
            cardStack.setAdapterIndex(videoIndex);
        }
        if (videoIndex == 0){
            undoButton.setVisibility(View.GONE);
        }
        adapter = new MainActivity.SwipeDeckAdapter();
        cardStack.setAdapter(adapter);

        manager.listener = new VideoManager.VideoManagerListener() {
            @Override
            public void onResults(List<SearchResult> results) {
                if (cardStack != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        };
        if (manager.videos.size() < 3){
            manager.getNextVideos();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("page", videoIndex);
    }

    public class SwipeDeckAdapter extends BaseAdapter {

        public SwipeDeckAdapter() {}

        @Override
        public int getCount() {
            return manager.videos.size();
        }

        @Override
        public Object getItem(int position) {
            return manager.videos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View v = convertView;
            if (v == null) {
                LayoutInflater inflater = getLayoutInflater();
                v = inflater.inflate(R.layout.video_card, parent, false);
            }

            SearchResult item = (SearchResult)getItem(position);

            final String videoId = item.getId().getVideoId();


            ImageView imageView = (ImageView) v.findViewById(R.id.offer_image);
            String url = item.getSnippet().getThumbnails().getHigh().getUrl();
            Picasso.with(context).load(url).fit().centerCrop().into(imageView);
            TextView textView = (TextView) v.findViewById(R.id.title_text);
            textView.setText(item.getSnippet().getTitle());
            textView = (TextView) v.findViewById(R.id.channel_text);
            textView.setText(item.getSnippet().getChannelTitle());
            textView = (TextView) v.findViewById(R.id.description_text);
            textView.setText(item.getSnippet().getDescription());
            Log.i(TAG, "getView: "+position+" "+item.toString());

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("Layer type: ", Integer.toString(v.getLayerType()));
                    Log.i("Hardware Accel type:", Integer.toString(View.LAYER_TYPE_HARDWARE));
                    Intent intent = YouTubeStandalonePlayer.createVideoIntent(
                            (Activity)v.getContext(), VideoManager.YOUTUBE_API_KEY, videoId, 0, true, false);
                    v.getContext().startActivity(intent);
                }
            });
            return v;
        }
    }
}
