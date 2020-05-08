package library.cameralibrary.fragment;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import library.imagelibrary.activity.ImageEditActivity;
import library.pickerlibrary.model.MediaData;
import library.pickerlibrary.selector.OnMediaSelector;
import library.videolibrary.activity.MultiVideoActivity;
import library.videolibrary.activity.VideoCutActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 普通选择器
 */
public class NormalMediaSelector implements OnMediaSelector {

    @Override
    public void onMediaSelect(@NonNull Context context, @NonNull List<MediaData> mediaDataList) {
        if (mediaDataList.size() <= 0) {
            return;
        }
        boolean isVideo = false;
        ArrayList<String> pathList = new ArrayList<>();
        for (MediaData mediaData : mediaDataList) {
            isVideo |= mediaData.isVideo();
            pathList.add(mediaData.getPath());
        }
        if (isVideo) {
            if (pathList.size() > 1) {
                Intent intent = new Intent(context, MultiVideoActivity.class);
                intent.putExtra(MultiVideoActivity.PATH, pathList);
                context.startActivity(intent);
            } else {
                Intent intent = new Intent(context, VideoCutActivity.class);
                intent.putExtra(VideoCutActivity.PATH, pathList.get(0));
                context.startActivity(intent);
            }
        } else {
            Intent intent = new Intent(context, ImageEditActivity.class);
            intent.putExtra(ImageEditActivity.IMAGE_PATH, pathList.get(0));
            context.startActivity(intent);
        }
    }
}
