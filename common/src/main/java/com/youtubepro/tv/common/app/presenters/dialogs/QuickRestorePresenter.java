package com.youtubepro.tv.common.app.presenters.dialogs;

import android.content.Context;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.youtubepro.tv.common.R;
import com.youtubepro.tv.common.app.models.playback.service.VideoStateService;
import com.youtubepro.tv.common.app.presenters.base.BasePresenter;
import com.youtubepro.tv.common.misc.BackupAndRestoreManager;
import com.youtubepro.tv.common.utils.AppDialogUtil;
import com.youtubepro.tv.common.utils.Utils;

public class QuickRestorePresenter extends BasePresenter<Void> {
    private static QuickRestorePresenter sInstance;

    public QuickRestorePresenter(Context context) {
        super(context);
    }

    public static QuickRestorePresenter instance(Context context) {
        if (sInstance == null) {
            sInstance = new QuickRestorePresenter(context);
        }

        sInstance.setContext(context);

        return sInstance;
    }

    public void unhold() {
        sInstance = null;
    }

    public void start() {
        BackupAndRestoreManager backupManager = new BackupAndRestoreManager(getContext());

        // NOTE: we don't have storage permission yet
        if (Utils.isFirstRun(getContext()) && backupManager.hasBackup()) {
            AppDialogUtil.showConfirmationDialog(getContext(), getContext().getString(R.string.app_restore), () -> {
                backupManager.checkPermAndRestore();
                MessageHelpers.showMessage(getContext(), R.string.msg_done);
            }, this::onFinish);
        } else {
            onFinish();
        }
    }
}
