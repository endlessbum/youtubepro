package com.youtubepro.tv.common.app.presenters.dialogs.menu.providers.channelgroup;

import android.content.Context;

import androidx.annotation.NonNull;

import com.liskovsoft.mediaserviceinterfaces.data.ItemGroup;
import com.youtubepro.tv.common.R;
import com.youtubepro.tv.common.app.models.data.Video;
import com.youtubepro.tv.common.app.presenters.AppDialogPresenter;
import com.youtubepro.tv.common.app.presenters.BrowsePresenter;
import com.youtubepro.tv.common.app.presenters.dialogs.menu.VideoMenuPresenter.VideoMenuCallback;
import com.youtubepro.tv.common.app.presenters.dialogs.menu.providers.ContextMenuProvider;
import com.youtubepro.tv.common.utils.SimpleEditDialog;

public class RenameGroupMenuProvider extends ContextMenuProvider {
    private final ChannelGroupServiceWrapper mService;

    public RenameGroupMenuProvider(@NonNull Context context, int idx) {
        super(context, idx);
        mService = ChannelGroupServiceWrapper.instance(context);
    }

    @Override
    public int getTitleResId() {
        return R.string.rename_group;
    }

    @Override
    public void onClicked(Video item, VideoMenuCallback callback) {
        AppDialogPresenter.instance(getContext()).closeDialog();
        SimpleEditDialog.show(
                getContext(),
                getContext().getString(R.string.rename_group),
                item.title,
                newValue -> {
                    item.title = newValue;
                    BrowsePresenter.instance(getContext()).renameSection(item);

                    ItemGroup channelGroup = mService.findChannelGroupById(item.channelGroupId);

                    if (channelGroup != null) {
                        //channelGroup.title = newValue;
                        //mService.addChannelGroup(channelGroup);
                        mService.renameChannelGroup(channelGroup, newValue);
                    }

                    return true;
                });
    }

    @Override
    public boolean isEnabled(Video item) {
        return item != null && item.channelGroupId != null;
    }

    @Override
    public int getMenuType() {
        return MENU_TYPE_SECTION;
    }
}
