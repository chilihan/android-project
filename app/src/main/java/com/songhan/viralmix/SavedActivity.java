package com.songhan.viralmix;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.squareup.picasso.Picasso;

public class SavedActivity extends AppCompatActivity {

    ListView listView;
    Cursor cursor;
    VideoDbAdapter dbAdapter;
    VideoCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved);
        dbAdapter = new VideoDbAdapter(this);
        dbAdapter.open();
        adapter = new VideoCursorAdapter(this,dbAdapter.getLiked(), 0);

        listView = (ListView)findViewById(R.id.video_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                Cursor cursor = (Cursor) adapter.getItem(position);
                VideoData item = VideoDbAdapter.getVideoFromCursor(cursor);
                Intent intent = YouTubeStandalonePlayer.createVideoIntent(
                        SavedActivity.this, VideoManager.YOUTUBE_API_KEY, item.videoId, 0, true, false);
                SavedActivity.this.startActivity(intent);
            }
        });
        listView.setAdapter(adapter);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        dbAdapter.close();
    }

    public class VideoCursorAdapter extends CursorAdapter {
        private LayoutInflater cursorInflater;

        // Default constructor
        public VideoCursorAdapter(Context context, Cursor cursor, int flags) {
            super(context, cursor, flags);
            cursorInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        }

        public void bindView(View v, Context context, Cursor cursor) {
            VideoData item = VideoDbAdapter.getVideoFromCursor(cursor);

            ImageView imageView = (ImageView) v.findViewById(R.id.offer_image);
            Picasso.with(context).load(item.imageUrl).fit().centerCrop().into(imageView);
            TextView textView = (TextView) v.findViewById(R.id.title_text);
            textView.setText(item.title);
            textView = (TextView) v.findViewById(R.id.channel_text);
            textView.setText(item.channel);
            textView = (TextView) v.findViewById(R.id.description_text);
            textView.setText(item.description);
        }

        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return cursorInflater.inflate(R.layout.video_list_item, parent, false);
        }
    }
}
