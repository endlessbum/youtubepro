package com.youtubepro.tv.ui.webbrowser;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.youtubepro.tv.common.app.presenters.WebBrowserPresenter;
import com.youtubepro.tv.common.app.views.WebBrowserView;
import com.youtubepro.tv.R;

public class WebBrowserFragment extends Fragment implements WebBrowserView {
    private WebView mWebView;
    private WebBrowserPresenter mWebBrowserPresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWebBrowserPresenter = WebBrowserPresenter.instance(getContext());
        mWebBrowserPresenter.setView(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            // Audit F-21: inflate with the parent container (correct LayoutParams)
            return inflater.inflate(R.layout.webbrowser, container, false);
        } catch (Exception e) { // Failed to load WebView provider: No WebView installed
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mWebView = (WebView) view.findViewById(R.id.webview);

        // Audit F-21: no WebView provider on this device - don't crash later on loadUrl
        if (mWebView == null) {
            return;
        }

        mWebView.setBackgroundColor(Color.TRANSPARENT);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Audit F-10: restrict navigation to http(s). file://, content:// and custom
        // schemes are never loaded inside the WebView.
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return !isWebUrl(url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return !isWebUrl(String.valueOf(request.getUrl()));
            }
        });

        // No caching
        //webSettings.setAppCacheEnabled(false);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
    }

    private static boolean isWebUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mWebBrowserPresenter.onViewInitialized();
    }

    @Override
    public void loadUrl(String url) {
        // Audit F-21: no NPE when the WebView provider is missing
        if (mWebView != null) {
            mWebView.loadUrl(url);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Audit F-21: don't leak the WebView across view recreations
        if (mWebView != null) {
            mWebView.destroy();
            mWebView = null;
        }
    }
}
