package com.youtubepro.tv.ui.browse.interfaces;

import com.youtubepro.tv.common.app.models.data.SettingsGroup;

public interface SettingsSection extends Section {
    void update(SettingsGroup items);
}
