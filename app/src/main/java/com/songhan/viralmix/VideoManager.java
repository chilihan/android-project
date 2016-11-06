package com.songhan.viralmix;

import android.os.AsyncTask;

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
    public static final int LOAD_COUNT = 3;
    private static VideoManager mInstance = null;
    private String nextPageToken = null;

    public interface VideoManagerListener {
        void onResults(List<SearchResult> results);
    }

    public ArrayList<SearchResult> videos;
    public VideoManagerListener listener;

    private YouTube youtube;

    private VideoManager(){
        videos = new ArrayList<>();

        try {
            youtube = new YouTube.Builder(
                    new com.google.api.client.http.javanet.NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(),
                    new HttpRequestInitializer() {
                        @Override
                        public void initialize(HttpRequest request) throws IOException {
                        }
                    }).setYouTubeRequestInitializer(new YouTubeRequestInitializer(YOUTUBE_API_KEY)).setApplicationName("ViralMix").build();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static VideoManager getInstance(){
        if(mInstance == null){
            mInstance = new VideoManager();
        }
        return mInstance;
    }

    public void getNextVideos(){
        new AsyncTask<Void, Void, List<SearchResult>>() {
            @Override
            protected List<SearchResult> doInBackground(Void... voids) {
                try {
                    YouTube.Search.List builder = youtube.search().list("snippet").setType("video").setMaxResults(Long.valueOf(LOAD_COUNT));
                    if (nextPageToken != null){
                        builder = builder.setPageToken(nextPageToken);
                    }
                    SearchListResponse result = builder.execute();
                    nextPageToken = result.getNextPageToken();
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
                videos.addAll(searchResults);
                if (listener != null)
                    listener.onResults(videos);
            }
        }.execute();
    }


    public void getNextVideosRelatedTo(final String videoId){
        new AsyncTask<Void, Void, List<SearchResult>>() {
            @Override
            protected List<SearchResult> doInBackground(Void... voids) {
                try {
                    return youtube.search().list("snippet").setType("video").setRelatedToVideoId(videoId).setMaxResults(Long.valueOf(LOAD_COUNT)).execute().getItems();
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
                videos.addAll(searchResults);
                if (listener != null)
                    listener.onResults(videos);
            }
        }.execute();
    }
}
