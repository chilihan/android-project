package com.songhan.viralmix;

import android.util.Log;
import android.widget.TextView;

import com.google.api.services.youtube.model.SearchResult;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Anne Han on 2016-11-19.
 */

public class VideoData {
    int id = -1;
    String videoId;
    String imageUrl;
    String title;
    String description;
    String channel;
    String nextPageId = null;
    int state = 0;

    public static final int STATE_NORMAL = 0;
    public static final int STATE_LIKED = 1;
    public static final int STATE_PASSED = 2;

    public VideoData(){}

    public VideoData(SearchResult item){
        this.videoId = item.getId().getVideoId();
        this.imageUrl = item.getSnippet().getThumbnails().getHigh().getUrl();
        this.title = item.getSnippet().getTitle();
        this.description = item.getSnippet().getDescription();
        this.channel = item.getSnippet().getChannelTitle();
    }
}
