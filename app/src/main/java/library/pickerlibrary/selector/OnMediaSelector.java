package library.pickerlibrary.selector;


import android.content.Context;

import androidx.annotation.NonNull;

import library.pickerlibrary.model.MediaData;

import java.util.List;

/**
 * 媒体选择器
 */
public interface OnMediaSelector {

    void onMediaSelect(@NonNull Context context, @NonNull List<MediaData> mediaDataList);
}
