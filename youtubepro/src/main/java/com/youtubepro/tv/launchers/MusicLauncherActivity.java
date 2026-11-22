package com.youtubepro.tv.launchers;

import android.os.Bundle;
import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import com.youtubepro.tv.common.app.presenters.BrowsePresenter;
import com.youtubepro.tv.common.misc.MotherActivity;

public class MusicLauncherActivity extends MotherActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BrowsePresenter.instance(this).selectSection(MediaGroup.TYPE_MUSIC);

        finish();
    }
}
