package com.youtubepro.tv.common.app.models.playback.listener;

import com.liskovsoft.mediaserviceinterfaces.data.MediaItemMetadata;
import com.youtubepro.tv.common.app.models.data.Video;
import com.youtubepro.tv.common.app.models.playback.manager.PlayerManager;
import com.youtubepro.tv.common.misc.TickleManager.TickleListener;

public interface PlayerEventListener extends PlayerUiEventListener, PlayerEngineEventListener, ViewEventListener, TickleListener {
    void onNewVideo(Video item);
    void onMetadata(MediaItemMetadata metadata);
    /**
     * Called after creation of {@link PlayerManager}
     */
    void onInit();
    void onFinish();
}
