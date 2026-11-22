package com.youtubepro.tv.common.app.views;

import com.youtubepro.tv.common.app.models.playback.manager.PlayerManager;
import com.youtubepro.tv.common.app.models.playback.listener.PlayerEventListener;

public interface PlaybackView extends PlayerManager {
    void showProgressBar(boolean show);
}
