package com.youtubepro.tv.common.app.views;

import com.youtubepro.tv.common.app.models.data.VideoGroup;
import com.youtubepro.tv.common.app.models.search.MediaServiceSearchTagProvider;
import com.youtubepro.tv.common.app.models.search.vineyard.Tag;

public interface SearchView {
    void updateSearch(VideoGroup group);
    void clearSearch();
    void clearSearchTags();
    void removeSearchTag(Tag tag);
    void setTagsProvider(MediaServiceSearchTagProvider provider);
    void showProgressBar(boolean show);
    void startSearch(String searchText);
    String getSearchText();
    void startVoiceRecognition();
    void finishReally();
}
