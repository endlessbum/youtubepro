package com.youtubepro.tv.common.misc;

import com.youtubepro.tv.common.app.models.data.Video;
import com.youtubepro.tv.common.app.models.data.VideoGroup;

public interface BrowseProcessor {
    interface OnItemReady {
        void onItemReady(Video video);
    }
    void process(VideoGroup videoGroup);
    void dispose();
}
