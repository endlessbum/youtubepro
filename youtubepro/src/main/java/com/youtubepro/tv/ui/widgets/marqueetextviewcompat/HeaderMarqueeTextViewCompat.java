package com.youtubepro.tv.ui.widgets.marqueetextviewcompat;

import android.content.Context;
import android.util.AttributeSet;

import com.youtubepro.tv.util.ViewUtil;

/**
 * MarqueeTextView used in browse section headers
 */
public class HeaderMarqueeTextViewCompat extends MarqueeTextViewCompat {
    public HeaderMarqueeTextViewCompat(Context context) {
        super(context);

        init();
    }

    public HeaderMarqueeTextViewCompat(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public HeaderMarqueeTextViewCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        ViewUtil.applyMarqueeRtlParams(this, true);
    }
}
