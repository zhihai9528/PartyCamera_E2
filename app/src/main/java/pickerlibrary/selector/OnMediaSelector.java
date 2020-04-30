package pickerlibrary.selector;


import android.content.Context;

import androidx.annotation.NonNull;

import pickerlibrary.model.MediaData;

import java.util.List;

import pickerlibrary.model.MediaData;

/**
 * 媒体选择器
 */
public interface OnMediaSelector {

    void onMediaSelect(@NonNull Context context, @NonNull List<MediaData> mediaDataList);
}
