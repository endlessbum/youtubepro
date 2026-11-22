package com.youtubepro.tv.common.app.models.search;

import com.youtubepro.tv.common.app.models.search.vineyard.Tag;

import java.util.List;

public interface SearchTagsProvider {
    interface ResultsCallback {
        void onResults(List<Tag> results);
    }
    void search(String query, ResultsCallback callback);
}
