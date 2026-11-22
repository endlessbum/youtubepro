package com.youtubepro.tv.common.exoplayer.controller;

import com.youtubepro.tv.common.app.models.data.Video;

public interface PlayerView {
    void setQualityInfo(String info);
    void setVideo(Video video);
}
