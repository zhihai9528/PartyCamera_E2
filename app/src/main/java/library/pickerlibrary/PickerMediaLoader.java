package library.pickerlibrary;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import library.pickerlibrary.loader.MediaLoader;

/**
 * 使用Glide加载缩略图
 */
class PickerMediaLoader implements MediaLoader {

    @Override
    public void loadThumbnail(@NonNull Context context, @NonNull ImageView imageView, @NonNull String path,
                              @DrawableRes int placeholder, @DrawableRes int error) {
        Glide.with(context)
                .asBitmap()
                .load(path)
                .apply(new RequestOptions()
                        .placeholder(placeholder)
                        .error(error)
                        .centerCrop())
                .into(imageView);
    }

    @Override
    public void loadThumbnail(@NonNull Context context, @NonNull ImageView imageView, @NonNull Uri path, int placeholder, int error) {
        Glide.with(context)
                .asBitmap()
                .load(path)
                .apply(new RequestOptions()
                        .placeholder(placeholder)
                        .error(error)
                        .centerCrop())
                .into(imageView);
    }

    @Override
    public void loadThumbnail(Context context, @NonNull ImageView imageView, @NonNull String path, int resize,
                              @DrawableRes int placeholder, @DrawableRes int error) {
        Glide.with(context)
                .asBitmap()
                .load(path)
                .apply(new RequestOptions()
                        .override(resize, resize)
                        .placeholder(placeholder)
                        .error(error)
                        .centerCrop())
                .into(imageView);
    }

    @Override
    public void loadImage(Context context, int width, int height, @NonNull ImageView imageView, @NonNull String path) {
        Glide.with(context)
                .load(path)
                .apply(new RequestOptions()
                        .override(width, height)
                        .priority(Priority.HIGH)
                        .fitCenter())
                .into(imageView);
    }

    @Override
    public void loadGifThumbnail(@NonNull Context context, @NonNull ImageView imageView, @NonNull String path, int resize,
                                 @DrawableRes int placeholder, @DrawableRes int error) {
        Glide.with(context)
                .asBitmap()
                .load(path)
                .apply(new RequestOptions()
                        .override(resize, resize)
                        .placeholder(placeholder)
                        .error(error)
                        .centerCrop())
                .into(imageView);
    }

    @Override
    public void loadGif(Context context, int width, int height, @NonNull ImageView imageView, @NonNull String path) {
        Glide.with(context)
                .asGif()
                .load(path)
                .apply(new RequestOptions()
                        .override(width, height)
                        .priority(Priority.HIGH)
                        .fitCenter())
                .into(imageView);
    }
}
