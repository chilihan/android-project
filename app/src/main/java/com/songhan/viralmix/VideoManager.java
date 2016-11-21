package com.songhan.viralmix;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anne Han on 2016-11-05.
 */

public class VideoManager {
    public static final String YOUTUBE_API_KEY = "AIzaSyAxbpe359seioYZR07ojRvcGm7tPJOWdyM";
    public static final String DEFAULT_VIDEO_ID = "ThisHoldsNextPageToken";
    public static final int LOAD_COUNT = 3;
    private VideoDbAdapter adapter;

    public interface VideoManagerListener {
        void onResults(List<VideoData> results);
    }

    public ArrayList<VideoData> videos;
    public VideoManagerListener listener;

    private YouTube youtube;
    private String query;

    public VideoManager(Context ctx, String query){
        videos = new ArrayList<>();
        adapter = new VideoDbAdapter(ctx);
        this.query = query;
        if (query != null){
            adapter.openTemp();
        } else {
            adapter.open();
        }
        if (adapter.getVideo(DEFAULT_VIDEO_ID) == null){
            //initialize
            VideoData v = new VideoData();
            v.imageUrl = v.title = v.description = v.channel = "A";
            v.videoId = DEFAULT_VIDEO_ID;
            v.state = VideoData.STATE_PASSED;
            adapter.insert(v);
        }

        try {
            youtube = new YouTube.Builder(
                    new com.google.api.client.http.javanet.NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(),
                    new HttpRequestInitializer() {
                        @Override
                        public void initialize(HttpRequest request) throws IOException {
                        }
                    }).setYouTubeRequestInitializer(new YouTubeRequestInitializer(YOUTUBE_API_KEY)).setApplicationName("ViralMix").build();
            try (Cursor cursor = adapter.getNormal()) {
                while (cursor.moveToNext()) {
                    videos.add(VideoDbAdapter.getVideoFromCursor(cursor));
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private int loadCount = 0;
    public void getNextVideos(final VideoData relatedVideo){
        new AsyncTask<Void, Void, List<SearchResult>>() {
            @Override
            protected List<SearchResult> doInBackground(Void... voids) {
                try {
                    YouTube.Search.List builder = youtube.search().list("snippet").setType("video").setMaxResults(Long.valueOf(LOAD_COUNT));
                    String pageToken = null;
                    VideoData v = relatedVideo;
                    if (v == null){
                        v = adapter.getVideo(DEFAULT_VIDEO_ID);
                        pageToken = v.nextPageId;
                    } else {
                        v = adapter.getVideo(v.videoId);
                        pageToken = v.nextPageId;
                        builder = builder.setRelatedToVideoId(v.videoId);
                    }
                    if (query != null){
                        builder = builder.setQ(query);
                    }
                    if (pageToken != null){
                        builder = builder.setPageToken(pageToken);
                    }
                    SearchListResponse result = builder.execute();
                    v.nextPageId = result.getNextPageToken();
                    save(v);
                    return result.getItems();
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                return new ArrayList<>();
            }

            @Override
            protected void onPostExecute(List<SearchResult> searchResults) {
                super.onPostExecute(searchResults);
                loadCount += 1;
                for (SearchResult r:searchResults) {
                    VideoData v = new VideoData(r);
                    if (!adapter.videoExist(v.videoId)){
                        videos.add(v);
                        adapter.insert(v);
                        loadCount = 5;
                    }
                }
                if (loadCount >= 5){
                    loadCount = 0;
                    if (listener != null)
                        listener.onResults(videos);
                } else {
                    getNextVideos(relatedVideo);
                }
            }
        }.execute();
    }

    public void getNextVideos() {
        Cursor liked = adapter.getLiked();
        int randomIndex = 0 + (int)(Math.random() * liked.getCount());
        if (randomIndex >= liked.getCount()){
            getNextVideos(null);
        } else {
            liked.moveToPosition(randomIndex);
            getNextVideos(VideoDbAdapter.getVideoFromCursor(liked));
        }
    }

    public void getNextVideosRelatedTo(VideoData v){
        getNextVideos(v);
    }

    public void save(VideoData v){
        if (v.id != -1){
            adapter.update(v.id, v);
        }
    }

    public void saveThrough(VideoData v){
        save(v);
        if (query != null){
            VideoDbAdapter defaultAdapter = new VideoDbAdapter(adapter.Ctx);
            defaultAdapter.open();
            if (defaultAdapter.videoExist(v.videoId)){
                v = defaultAdapter.getVideo(v.videoId);
                defaultAdapter.update(v.id, v);
            } else {
                defaultAdapter.insert(v);
            }
            defaultAdapter.close();
        }
    }

    public void close(){
        adapter.close();
    }
}
