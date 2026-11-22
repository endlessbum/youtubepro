package com.youtubepro.tv.launchers;

import android.os.Bundle;
import com.youtubepro.tv.common.app.presenters.SearchPresenter;
import com.youtubepro.tv.common.misc.MotherActivity;

public class SearchLauncherActivity extends MotherActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SearchPresenter.instance(this).startSearch(null);

        finish();
    }
}
