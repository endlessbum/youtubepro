package com.youtubepro.tv.ui.channel;

import android.os.Bundle;
import com.youtubepro.tv.R;
import com.youtubepro.tv.ui.common.LeanbackActivity;

public class ChannelActivity extends LeanbackActivity {
    private static final String TAG = ChannelActivity.class.getSimpleName();
    private ChannelFragment mFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_channel);
        mFragment = (ChannelFragment) getSupportFragmentManager().findFragmentById(R.id.channel_fragment);
    }

    @Override
    public void finishReally() {
        super.finishReally();

        mFragment.onFinish();
    }
}
