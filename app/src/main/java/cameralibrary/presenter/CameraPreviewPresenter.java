package cameralibrary.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import cameralibrary.activity.CameraSettingActivity;
import cameralibrary.camera.CameraController;
import cameralibrary.camera.CameraParam;
import cameralibrary.camera.ICameraController;
import cameralibrary.camera.OnFrameAvailableListener;
import cameralibrary.camera.OnSurfaceTextureListener;
import cameralibrary.camera.PreviewCallback;
import cameralibrary.fragment.CameraPreviewFragment;
import cameralibrary.listener.OnCaptureListener;
import cameralibrary.listener.OnFpsListener;
import cameralibrary.listener.OnPreviewCaptureListener;
import cameralibrary.render.CameraRenderer;
import cameralibrary.utils.PathConstraints;

import filterlibrary.glfilter.color.bean.DynamicColor;
import filterlibrary.glfilter.makeup.bean.DynamicMakeup;
import filterlibrary.glfilter.resource.FilterHelper;
import filterlibrary.glfilter.resource.ResourceHelper;
import filterlibrary.glfilter.resource.ResourceJsonCodec;
import filterlibrary.glfilter.resource.bean.ResourceData;
import filterlibrary.glfilter.resource.bean.ResourceType;
import filterlibrary.glfilter.stickers.bean.DynamicSticker;

import utilslibrary.utils.BitmapUtils;
import utilslibrary.utils.BrightnessUtils;
import utilslibrary.utils.FileUtils;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 预览的presenter
 * @author CainHuang
 * @date 2019/7/3
 */
public class CameraPreviewPresenter extends PreviewPresenter<CameraPreviewFragment>
        implements PreviewCallback, OnCaptureListener, OnFpsListener,
        OnSurfaceTextureListener, OnFrameAvailableListener{

    private static final String TAG = "CameraPreviewPresenter";

    // 当前索引
    private int mFilterIndex = 0;

    // 预览参数
    private CameraParam mCameraParam;

    private Activity mActivity;

    // 背景音乐
    private String mMusicPath;


    // 当前录制进度
    private float mCurrentProgress;
    // 最大时长
    private long mMaxDuration;
    // 剩余时长
    private long mRemainDuration;

    // 相机接口
    private ICameraController mCameraController;

    // 渲染器
    private final CameraRenderer mCameraRenderer;

    public CameraPreviewPresenter(CameraPreviewFragment target) {
        super(target);
        mCameraParam = CameraParam.getInstance();

        mCameraRenderer = new CameraRenderer(this);

    }

    public void onAttach(Activity activity) {
        mActivity = activity;


        mCameraRenderer.initRenderer();

//        // 备注：目前支持CameraX的渲染流程，但CameraX回调预览帧数据有些问题，人脸关键点SDK检测返回的数据错乱，暂不建议在商用项目中使用CameraX
//        if (CameraApi.hasCamera2(mActivity)) {
//            mCameraController = new CameraXController(getTarget(), ContextCompat.getMainExecutor(mActivity));
//        } else {
//            mCameraController = new CameraController(mActivity);
//        }
        mCameraController = new CameraController(mActivity);
        mCameraController.setPreviewCallback(this);
        mCameraController.setOnFrameAvailableListener(this);
        mCameraController.setOnSurfaceTextureListener(this);

        if (BrightnessUtils.getSystemBrightnessMode(mActivity) == 1) {
            mCameraParam.brightness = -1;
        } else {
            mCameraParam.brightness = BrightnessUtils.getSystemBrightness(mActivity);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        openCamera();
        mCameraParam.captureCallback = this;
        mCameraParam.fpsCallback = this;
    }

    @Override
    public void onPause() {
        super.onPause();
        mCameraRenderer.onPause();
        closeCamera();
        mCameraParam.captureCallback = null;
        mCameraParam.fpsCallback = null;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 销毁人脸检测器

    }

    public void onDetach() {
        mActivity = null;
        mCameraRenderer.destroyRenderer();
    }

    @NonNull
    @Override
    public Context getContext() {
        return mActivity;
    }



    @Override
    public void onSurfaceCreated(SurfaceTexture surfaceTexture) {
        mCameraRenderer.onSurfaceCreated(surfaceTexture);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        mCameraRenderer.onSurfaceChanged(width, height);
    }

    @Override
    public void onSurfaceDestroyed() {
        mCameraRenderer.onSurfaceDestroyed();
    }

    @Override
    public void changeResource(@NonNull ResourceData resourceData) {
        ResourceType type = resourceData.type;
        String unzipFolder = resourceData.unzipFolder;
        if (type == null) {
            return;
        }
        try {
            switch (type) {
                // 单纯的滤镜
                case FILTER: {
                    String folderPath = ResourceHelper.getResourceDirectory(mActivity) + File.separator + unzipFolder;
                    DynamicColor color = ResourceJsonCodec.decodeFilterData(folderPath);
                    mCameraRenderer.changeResource(color);
                    break;
                }

                // 贴纸
                case STICKER: {
                    String folderPath = ResourceHelper.getResourceDirectory(mActivity) + File.separator + unzipFolder;
                    DynamicSticker sticker = ResourceJsonCodec.decodeStickerData(folderPath);
                    mCameraRenderer.changeResource(sticker);
                    break;
                }

                // TODO 多种结果混合
                case MULTI: {
                    break;
                }

                // 所有数据均为空
                case NONE: {
                    mCameraRenderer.changeResource((DynamicSticker) null);
                    break;
                }

                default:
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "parseResource: ", e);
        }
    }

    @Override
    public void changeDynamicFilter(DynamicColor color) {
        mCameraRenderer.changeFilter(color);
    }

    @Override
    public void changeDynamicMakeup(DynamicMakeup makeup) {
        mCameraRenderer.changeMakeup(makeup);
    }

    @Override
    public void changeDynamicFilter(int filterIndex) {
        if (mActivity == null) {
            return;
        }
        String folderPath = FilterHelper.getFilterDirectory(mActivity) + File.separator +
                FilterHelper.getFilterList().get(filterIndex).unzipFolder;
        DynamicColor color = null;
        if (!FilterHelper.getFilterList().get(filterIndex).unzipFolder.equalsIgnoreCase("none")) {
            try {
                color = ResourceJsonCodec.decodeFilterData(folderPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mCameraRenderer.changeFilter(color);
    }

    @Override
    public int previewFilter() {
        mFilterIndex--;
        if (mFilterIndex < 0) {
            int count = FilterHelper.getFilterList().size();
            mFilterIndex = count > 0 ? count - 1 : 0;
        }
        changeDynamicFilter(mFilterIndex);
        return mFilterIndex;
    }

    @Override
    public int nextFilter() {
        mFilterIndex++;
        mFilterIndex = mFilterIndex % FilterHelper.getFilterList().size();
        changeDynamicFilter(mFilterIndex);
        return mFilterIndex;
    }

    @Override
    public int getFilterIndex() {
        return mFilterIndex;
    }

    @Override
    public void showCompare(boolean enable) {
        mCameraParam.showCompare = enable;
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
        mCameraRenderer.setTextureSize(width, height);
    }

    /**
     * 关闭相机
     */
    private void closeCamera() {
        mCameraController.closeCamera();
    }

    @Override
    public void takePicture() {
        mCameraRenderer.takePicture();
    }

    @Override
    public void switchCamera() {
        mCameraController.switchCamera();
    }



    @Override
    public void enableEdgeBlurFilter(boolean enable) {
        mCameraRenderer.changeEdgeBlur(enable);
    }

    @Override
    public void setMusicPath(String path) {
        mMusicPath = path;
    }

    @Override
    public void onOpenCameraSettingPage() {
        if (mActivity != null) {
            Intent intent = new Intent(mActivity, CameraSettingActivity.class);
            mActivity.startActivity(intent);
        }
    }

    /**
     * 相机打开回调
     */
    public void onCameraOpened() {
        Log.d(TAG, "onCameraOpened: " +
                "orientation - " + mCameraController.getOrientation()
                + "width - " + mCameraController.getPreviewWidth()
                + ", height - " + mCameraController.getPreviewHeight());
                        mCameraController.getOrientation(),
                        mCameraController.getPreviewWidth(),
                        mCameraController.getPreviewHeight());
    }

    // ------------------------- Camera 输出SurfaceTexture准备完成回调 -------------------------------
    @Override
    public void onSurfaceTexturePrepared(@NonNull SurfaceTexture surfaceTexture) {
        onCameraOpened();
        mCameraRenderer.bindInputSurfaceTexture(surfaceTexture);
    }

    // ---------------------------------- 相机预览数据回调 ------------------------------------------
    @Override
    public void onPreviewFrame(byte[] data) {
        Log.d(TAG, "onPreviewFrame: width - " + mCameraController.getPreviewWidth()
                + ", height - " + mCameraController.getPreviewHeight());
                        mCameraController.getPreviewHeight();
    }



    // ------------------------------ SurfaceTexture帧可用回调 --------------------------------------
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//        mCameraRenderer.requestRender();
    }


    // ------------------------------------ 拍照截屏回调 --------------------------------------------

    @Override
    public void onCapture(Bitmap bitmap) {
        String filePath = PathConstraints.getImageCachePath(mActivity);
        BitmapUtils.saveBitmap(filePath, bitmap);
        if (mCameraParam.captureListener != null) {
            mCameraParam.captureListener.onMediaSelectedListener(filePath, OnPreviewCaptureListener.MediaTypePicture);
        }
    }

    // ------------------------------------ 渲染fps回调 ------------------------------------------
    /**
     * fps数值回调
     * @param fps
     */
    @Override
    public void onFpsCallback(float fps) {
        if (getTarget() != null) {
            getTarget().showFps(fps);
        }
    }
}
