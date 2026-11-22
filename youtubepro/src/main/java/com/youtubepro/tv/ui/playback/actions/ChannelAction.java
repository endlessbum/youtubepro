package com.youtubepro.tv.ui.playback.actions;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.leanback.widget.Action;
import com.youtubepro.tv.R;

/**
 * An action for displaying a channel icon.
 */
public class ChannelAction extends PaddingAction {
    public ChannelAction(Context context) {
        super(R.id.action_channel);
        Drawable uncoloredDrawable = ContextCompat.getDrawable(context, R.drawable.action_channel);

        setIcon(uncoloredDrawable);
        setLabel1(context.getString(
                R.string.action_channel));
    }
}
