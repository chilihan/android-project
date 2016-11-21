package com.songhan.viralmix;

import com.daprlabs.aaron.swipedeck.SwipeDeck;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.google.api.services.youtube.model.Video;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private MainActivity context = this;
    private SwipeDeck cardStack;
    private SwipeDeckAdapter adapter = new SwipeDeckAdapter();
    private VideoManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cardStack = (SwipeDeck) findViewById(R.id.swipe_deck);

        cardStack.setCallback(new SwipeDeck.SwipeDeckCallback() {
            @Override
            public void cardSwipedLeft(long stableId) {
                Log.i("MainActivity", "card was swiped left, position in adapter: " + stableId);
                if (stableId + 3 > manager.videos.size()) {
                    manager.getNextVideos();
                }
                VideoData v = manager.videos.get((int)stableId);
                v.state = VideoData.STATE_PASSED;
                manager.saveThrough(v);
            }

            @Override
            public void cardSwipedRight(long stableId) {
                Log.i("MainActivity", "card was swiped right, position in adapter: " + stableId);
                VideoData v = manager.videos.get((int)stableId);
                v.state = VideoData.STATE_LIKED;
                manager.saveThrough(v);
                manager.getNextVideosRelatedTo(v);
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

        cardStack.setAdapter(adapter);
        reload(null);
    }

    public void reload(String query){
        if (manager != null){
            manager.listener = null;
        }
        manager = new VideoManager(this, query);
        manager.listener = new VideoManager.VideoManagerListener() {
            @Override
            public void onResults(List<VideoData> results) {
                if (cardStack != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        };
        if (manager.videos.size() < 3){
            manager.getNextVideos();
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();
        search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                reload(query.trim().isEmpty() ? null : query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                reload(query.trim().isEmpty() ? null : query);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_liked_videos:
                Intent startIntent = new Intent(this, SavedActivity.class);
                startActivity(startIntent);
                break;
            case R.id.action_reset:
                SharedPreferences preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("DATABASE_VERSION", preferences.getInt("DATABASE_VERSION", 10) + 1);
                editor.apply();
                finish();
                startActivity(getIntent());
                break;
        }
        return true;
    }

    public class SwipeDeckAdapter extends BaseAdapter {

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

            VideoData item = (VideoData)getItem(position);

            final String videoId = item.videoId;


            ImageView imageView = (ImageView) v.findViewById(R.id.offer_image);
            Picasso.with(context).load(item.imageUrl).fit().centerCrop().into(imageView);
            TextView textView = (TextView) v.findViewById(R.id.title_text);
            textView.setText(item.title);
            textView = (TextView) v.findViewById(R.id.channel_text);
            textView.setText(item.channel);
            textView = (TextView) v.findViewById(R.id.description_text);
            textView.setText(item.description);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = YouTubeStandalonePlayer.createVideoIntent(
                            MainActivity.this, VideoManager.YOUTUBE_API_KEY, videoId, 0, true, false);
                    MainActivity.this.startActivity(intent);
                }
            });
            return v;
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        manager.close();
    }
}
