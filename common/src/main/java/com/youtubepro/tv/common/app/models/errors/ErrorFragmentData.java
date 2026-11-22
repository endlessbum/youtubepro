package com.youtubepro.tv.common.app.models.errors;

public interface ErrorFragmentData {
    void onAction();
    String getMessage();
    String getActionText();
}
