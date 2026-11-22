package com.youtubepro.tv.common.app.views;

import com.youtubepro.tv.common.app.models.data.VideoGroup;

public interface ChannelUploadsView {
    void update(VideoGroup videoGroup);
    void showProgressBar(boolean show);
    void clear();
}
