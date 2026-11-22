package com.youtubepro.tv.common.app.views;

import com.youtubepro.tv.common.app.models.data.VideoGroup;

public interface ChannelView {
    void update(VideoGroup videoGroup);
    void setPosition(int index);
    void showProgressBar(boolean show);
    void clear();
}
