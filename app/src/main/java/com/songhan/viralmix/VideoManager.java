package com.songhan.viralmix;

import android.os.AsyncTask;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anne Han on 2016-11-05.
 */

public class VideoManager {
    public static final String YOUTUBE_API_KEY = "AIzaSyAxbpe359seioYZR07ojRvcGm7tPJOWdyM";
    private static VideoManager mInstance = null;

    private List<SearchResult> results;
    public VideoManagerListener listener;

    private VideoManager(){
    }

    public static VideoManager getInstance(){
        if(mInstance == null){
            mInstance = new VideoManager();
        }
        return mInstance;
    }

    public interface VideoManagerListener {
        public void onResults(List<SearchResult> results);
    }

    public void getVideos(){
        if (results != null){
            if (listener != null)
                listener.onResults(results); // <---- fire listener here
            return;
        }
        new AsyncTask<Void, Void, List<SearchResult>>() {
            @Override
            protected List<SearchResult> doInBackground(Void... voids) {
                try {
                    YouTube youtube = new YouTube.Builder(
                            new com.google.api.client.http.javanet.NetHttpTransport(),
                            JacksonFactory.getDefaultInstance(),
                            new HttpRequestInitializer() {
                                @Override
                                public void initialize(HttpRequest request) throws IOException {
                                }
                            }).setYouTubeRequestInitializer(new YouTubeRequestInitializer(YOUTUBE_API_KEY)).setApplicationName("ViralMix").build();
                    return youtube.search().list("snippet").setType("video").setMaxResults(Long.valueOf(10)).execute().getItems();
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                return new ArrayList<SearchResult>();
            }

            @Override
            protected void onPostExecute(List<SearchResult> searchResults) {
                super.onPostExecute(searchResults);
                results = searchResults;
                if (listener != null)
                    listener.onResults(results); // <---- fire listener here
            }
        }.execute();

    }
}
