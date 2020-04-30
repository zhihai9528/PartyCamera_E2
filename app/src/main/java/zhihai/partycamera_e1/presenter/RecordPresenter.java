package zhihai.partycamera_e1.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.os.Environment;
import android.os.FileUtils;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import zhihai.partycamera_e1.SpeedRecordActivity;
import cameralibrary.camera.CameraApi;
import cameralibrary.camera.CameraController;
import cameralibrary.camera.CameraXController;
import cameralibrary.camera.ICameraController;
import cameralibrary.camera.OnFrameAvailableListener;
import cameralibrary.camera.OnSurfaceTextureListener;
import cameralibrary.utils.PathConstraints;


import java.io.File;
import java.util.ArrayList;
import java.util.List;




/**
 * 录制器的presenter
 * @author CainHuang
 * @date 2019/7/7
 */
public class RecordPresenter implements OnSurfaceTextureListener, OnFrameAvailableListener {

    private SpeedRecordActivity mActivity;

    // 音视频参数
    //private final VideoParams mVideoParams;
    //private final AudioParams mAudioParams;
    // 录制操作开始
    private boolean mOperateStarted = false;

    // 当前录制进度
    private float mCurrentProgress;
    // 最大时长
    private long mMaxDuration;
    // 剩余时长
    private long mRemainDuration;

    // 视频录制器
    //private HWMediaRecorder mHWMediaRecorder;

    // 视频列表
    //private List<MediaInfo> mVideoList = new ArrayList<>();

    // 录制音频信息
    //private RecordInfo mAudioInfo;
    // 录制视频信息
    //private RecordInfo mVideoInfo;

    // 命令行编辑器
    //private CommandEditor mCommandEditor;

    // 相机控制器
    private final ICameraController mCameraController;

    public RecordPresenter(SpeedRecordActivity activity) {
        mActivity = activity;

        // 视频录制器
        // mHWMediaRecorder = new HWMediaRecorder(this);

        // 视频参数
        //mVideoParams = new VideoParams();
        //mVideoParams.setVideoPath(getVideoTempPath(mActivity));

        // 音频参数
        //mAudioParams = new AudioParams();
        // mAudioParams.setAudioPath(getAudioTempPath(mActivity));

        // 命令行编辑器
        //mCommandEditor = new CommandEditor();

        // 创建相机控制器
        if (CameraApi.hasCamera2(mActivity)) {
            mCameraController = new CameraXController(activity, ContextCompat.getMainExecutor(activity));
        } else {
            mCameraController = new CameraController(activity);
        }
        mCameraController.setOnFrameAvailableListener(this);
        mCameraController.setOnSurfaceTextureListener(this);
    }

    /**
     * 启动
     */
    public void onResume() {
        openCamera();
    }

    /**
     * 暂停
     */
    public void onPause() {
        closeCamera();
    }

    /**
     * 切换相机
     */
    public void switchCamera() {
        mCameraController.switchCamera();
    }

    /**
     * 释放资源
     */
    public void release() {
        mActivity = null;

    }


    @Override
    public void onSurfaceTexturePrepared(@NonNull SurfaceTexture surfaceTexture) {
        mActivity.bindSurfaceTexture(surfaceTexture);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mActivity.onFrameAvailable();
    }


    /**
     * 打开相机
     */
    private void openCamera() {
        mCameraController.openCamera();
        calculateImageSize();
    }

    /**
     * 计算imageView 的宽高
     */
    private void calculateImageSize() {
        int width;
        int height;
        if (mCameraController.getOrientation() == 90 || mCameraController.getOrientation() == 270) {
            width = mCameraController.getPreviewHeight();
            height = mCameraController.getPreviewWidth();
        } else {
            width = mCameraController.getPreviewWidth();
            height = mCameraController.getPreviewHeight();
        }
        mActivity.updateTextureSize(width, height);
    }

    /**
     * 释放资源
     */
    private void closeCamera() {
        mCameraController.closeCamera();
    }


    /**
     * 获取绑定的Activity
     * @return
     */
    public Activity getActivity() {
        return mActivity;
    }


    /**
     * 创建合成的视频文件名
     * @return
     */
    public String generateOutputPath() {
        return PathConstraints.getVideoCachePath(mActivity);
    }

    /**
     * 获取音频缓存绝对路径
     * @param context
     * @return
     */
    private static String getAudioTempPath(@NonNull Context context) {
        String directoryPath;
        // 判断外部存储是否可用，如果不可用则使用内部存储路径
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            directoryPath = context.getExternalCacheDir().getAbsolutePath();
        } else { // 使用内部存储缓存目录
            directoryPath = context.getCacheDir().getAbsolutePath();
        }
        String path = directoryPath + File.separator + "temp.aac";
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return path;
    }

    /**
     * 获取视频缓存绝对路径
     * @param context
     * @return
     */
    private static String getVideoTempPath(@NonNull Context context) {
        String directoryPath;
        // 判断外部存储是否可用，如果不可用则使用内部存储路径
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && context.getExternalCacheDir() != null) {
            directoryPath = context.getExternalCacheDir().getAbsolutePath();
        } else { // 使用内部存储缓存目录
            directoryPath = context.getCacheDir().getAbsolutePath();
        }
        String path = directoryPath + File.separator + "temp.mp4";
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return path;
    }

}
