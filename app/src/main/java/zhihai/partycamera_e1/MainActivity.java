package zhihai.partycamera_e1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import cameralibrary.PreviewEngine;
import cameralibrary.model.AspectRatio;

import filterlibrary.glfilter.resource.FilterHelper;
import filterlibrary.glfilter.resource.ResourceHelper;
import utilslibrary.utils.PermissionUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE = 0;

    private static final int DELAY_CLICK = 500;

    private boolean mOnClick;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        initView();
        if (PermissionUtils.permissionChecking(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            initResources();
        }
        mHandler = new Handler();
    }

    private void checkPermissions() {
        PermissionUtils.requestPermissions(this, new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        }, REQUEST_CODE);
    }

    private void initView() {
        findViewById(R.id.btn_camera).setOnClickListener(this);
        findViewById(R.id.btn_edit_video).setOnClickListener(this);
        findViewById(R.id.btn_edit_picture).setOnClickListener(this);
        findViewById(R.id.btn_speed_record).setOnClickListener(this);
        findViewById(R.id.btn_edit_music_merge).setOnClickListener(this);
        findViewById(R.id.btn_ff_media_record).setOnClickListener(this);
        findViewById(R.id.btn_music_player).setOnClickListener(this);
        findViewById(R.id.btn_video_player).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mOnClick = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                initResources();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (mOnClick) {
            return;
        }
        mOnClick = true;
        mHandler.postDelayed(() -> {
            mOnClick = false;
        }, DELAY_CLICK);
        switch (v.getId()) {
            case R.id.btn_camera: {
                previewCamera();
                break;
            }
/*
            case R.id.btn_edit_video: {
                scanMedia(false,true);
                break;
            }

            case R.id.btn_edit_picture: {
                scanMedia(true, false);
                break;
            }

 */

            case R.id.btn_speed_record: {
                Intent intent = new Intent(MainActivity.this, SpeedRecordActivity.class);
                startActivity(intent);
                break;
            }
/*
            case R.id.btn_edit_music_merge: {
                musicMerge();
                break;
            }

            case R.id.btn_ff_media_record: {
                ffmpegRecord();
                break;
            }

            case R.id.btn_music_player: {
                musicPlayerTest();
                break;
            }

            case R.id.btn_video_player: {
                videoPlayerTest();
                break;
            }

 */
        }
    }

    /**
     * 初始化资源列表以及滤镜等资源，（可添加贴纸资源）
     */
    private void initResources() {
        new Thread(() -> {
            ResourceHelper.initAssetsResource(MainActivity.this);   //初始化资源列表
            FilterHelper.initAssetsFilter(MainActivity.this);  //初始化滤镜资源
        }).start();
    }

    /**
     * 打开预览页面
     */
    private void previewCamera() {
        if (PermissionUtils.permissionChecking(this, Manifest.permission.CAMERA)) {
            PreviewEngine.from(this)
                    .setCameraRatio(AspectRatio.Ratio_16_9)
                    .showFacePoints(false)
                    .showFps(true)
                    .backCamera(true)
                    .setPreviewCaptureListener((path, type) -> {
                        /*
                        if (type == OnPreviewCaptureListener.MediaTypePicture) {
                            Intent intent = new Intent(MainActivity.this, ImageEditActivity.class);
                            intent.putExtra(ImageEditActivity.IMAGE_PATH, path);
                            intent.putExtra(ImageEditActivity.DELETE_INPUT_FILE, true);
                            startActivity(intent);
                        }

                         */
                        /*
                        else if (type == OnPreviewCaptureListener.MediaTypeVideo) {
                            Intent intent = new Intent(MainActivity.this, VideoEditActivity.class);
                            intent.putExtra(VideoEditActivity.VIDEO_PATH, path);
                            startActivity(intent);
                        }

                         */
                    })
                    .startPreview();
        } else {
            checkPermissions();
        }
    }

    /**
     * 扫描媒体库
     *
     * @param enableImage
     * @param enableVideo
     */
/*
    private void scanMedia(boolean enableImage, boolean enableVideo) {
        MediaPicker.from(this)
                .showImage(enableImage)
                .showVideo(enableVideo)
                .setMediaSelector(new NormalMediaSelector())
                .show();
    }

 */
}


