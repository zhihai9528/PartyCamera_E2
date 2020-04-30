package cameralibrary.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;


import cameralibrary.camera.CameraParam;
import cameralibrary.loader.impl.CameraMediaLoader;
import cameralibrary.model.GalleryType;
import cameralibrary.presenter.CameraPreviewPresenter;

import cameralibrary.widget.CameraMeasureFrameLayout;
import cameralibrary.widget.CameraPreviewTopbar;
import cameralibrary.widget.PreviewMeasureListener;
import cameralibrary.widget.RecordButton;
import cameralibrary.widget.RecordCountDownView;
import cameralibrary.widget.RecordProgressView;
import cameralibrary.widget.RecordSpeedLevelBar;


import zhihai.partycamera_e1.R;
import pickerlibrary.loader.AlbumDataLoader;
import pickerlibrary.model.AlbumData;

import utilslibrary.bean.MusicData;
import utilslibrary.dialog.DialogBuilder;
import utilslibrary.fragment.MusicPickerFragment;
import utilslibrary.fragment.PermissionErrorDialogFragment;
import utilslibrary.utils.BrightnessUtils;
import utilslibrary.utils.PermissionUtils;
import utilslibrary.widget.RoundOutlineProvider;
import widgetlibrary.widget.CameraTabView;


/**
 * 相机预览页面
 */
public class CameraPreviewFragment extends Fragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "CameraPreviewFragment";
    private static final boolean VERBOSE = true;

    private static final String FRAGMENT_TAG = "FRAGMENT_TAG";
    private static final String FRAGMENT_DIALOG = "dialog";

    private static final int ALBUM_LOADER_ID = 1;

    // 预览参数
    private CameraParam mCameraParam;

    // Fragment主页面
    private View mContentView;
    // 预览部分
    private CameraMeasureFrameLayout mPreviewLayout;
    private TextureView mCameraTextureView;
    // fps显示
    private TextView mFpsView;

    // 顶部topbar
    private CameraPreviewTopbar mPreviewTopbar;

    // 速度选择条
    private RecordSpeedLevelBar mSpeedBar;
    private boolean mSpeedBarShowing;

    // 录制按钮
    private RecordButton mBtnRecord;

    private View mLayoutMedia;


    // 刪除布局
    private LinearLayout mLayoutDelete;

    // 相机指示器
    private CameraTabView mCameraTabView;
    private View mTabIndicator;

    private boolean mFragmentAnimating;
    private FrameLayout mFragmentContainer;
    // 贴纸资源页面
    private PreviewResourceFragment mResourcesFragment;
    // 滤镜页面
    private PreviewEffectFragment mEffectFragment;
    // 更多设置界面
    private PreviewSettingFragment mSettingFragment;

    private final Handler mMainHandler;
    private Activity mActivity;

    private CameraPreviewPresenter mPreviewPresenter;

    // 本地缩略图加载器
    private LoaderManager mLocalImageLoader;

    // 当前对话框
    private Dialog mDialog;

    public CameraPreviewFragment() {
        mCameraParam = CameraParam.getInstance();
        mMainHandler = new Handler(Looper.getMainLooper());
        mPreviewPresenter = new CameraPreviewPresenter(this);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        } else {
            mActivity = getActivity();
        }
        mPreviewPresenter.onAttach(mActivity);
        Log.d(TAG, "onAttach: ");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreviewPresenter.onCreate();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_camera_preview, container, false);
        return mContentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isCameraEnable()) {
            initView(mContentView);
        } else {
            PermissionUtils.requestCameraPermission(this);
        }

        if (PermissionUtils.permissionChecking(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            mLocalImageLoader = LoaderManager.getInstance(this);
            mLocalImageLoader.initLoader(ALBUM_LOADER_ID, null, this);
        }
    }

    /**
     * 初始化页面
     * @param view
     */
    private void initView(View view) {
        initPreviewSurface();
        initPreviewTopbar();
        initCameraTabView();
    }

    private void initPreviewSurface() {
        mFpsView = mContentView.findViewById(R.id.tv_fps);
        mPreviewLayout = mContentView.findViewById(R.id.layout_camera_preview);
        mCameraTextureView = new TextureView(mActivity);
        //mCameraTextureView.addOnTouchScroller(mTouchScroller);
        //mCameraTextureView.addMultiClickListener(mMultiClickListener);
        mCameraTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        mPreviewLayout.addView(mCameraTextureView);

        // 添加圆角显示
        if (Build.VERSION.SDK_INT >= 21) {
            mCameraTextureView.setOutlineProvider(new RoundOutlineProvider(getResources().getDimension(R.dimen.dp7)));
            mCameraTextureView.setClipToOutline(true);
        }
        mPreviewLayout.setOnMeasureListener(new PreviewMeasureListener(mPreviewLayout));
    }

    /**
     * 初始化顶部topbar
     */
    private void initPreviewTopbar() {
        mPreviewTopbar = mContentView.findViewById(R.id.camera_preview_topbar);
        mPreviewTopbar.addOnCameraCloseListener(this::closeCamera)
                .addOnCameraSwitchListener(this::switchCamera)
                .addOnShowPanelListener(type -> {
                    switch (type) {

                        /*
                        case CameraPreviewTopbar.PanelMusic: {
                            openMusicPicker();
                            break;
                        }

                         */

                        case CameraPreviewTopbar.PanelSpeedBar: {
                            setShowingSpeedBar(mSpeedBar.getVisibility() != View.VISIBLE);
                            break;
                        }

                        case CameraPreviewTopbar.PanelFilter: {
                            showEffectFragment();
                            break;
                        }

                        case CameraPreviewTopbar.PanelSetting: {
                            showSettingFragment();
                            break;
                        }
                    }
                });
    }



    /**
     * 初始化相机底部tab view
     */
    private void initCameraTabView() {
        mTabIndicator = mContentView.findViewById(R.id.iv_tab_indicator);
        mCameraTabView = mContentView.findViewById(R.id.tl_camera_tab);

        mCameraTabView.addTab(mCameraTabView.newTab().setText(R.string.tab_picture));
        mCameraTabView.addTab(mCameraTabView.newTab().setText(R.string.tab_video_60s));
        mCameraTabView.addTab(mCameraTabView.newTab().setText(R.string.tab_video_15s), true);
        mCameraTabView.addTab(mCameraTabView.newTab().setText(R.string.tab_video_picture));

        mCameraTabView.setIndicateCenter(true);
        mCameraTabView.setScrollAutoSelected(true);
        mCameraTabView.addOnTabSelectedListener(new CameraTabView.OnTabSelectedListener() {
            @Override
            public void onTabSelected(CameraTabView.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    mCameraParam.mGalleryType = GalleryType.PICTURE;
                    if (!isStorageEnable()) {
                        PermissionUtils.requestRecordSoundPermission(CameraPreviewFragment.this);
                    }
                    if (mBtnRecord != null) {
                        mBtnRecord.setRecordEnable(false);
                    }
                } else if (position == 1) {
                    mCameraParam.mGalleryType = GalleryType.VIDEO_60S;

                    }
                    if (mBtnRecord != null) {
                        mBtnRecord.setRecordEnable(true);
                    }
                    mPreviewPresenter.setRecordSeconds(60);
                }

            @Override
            public void onTabUnselected(CameraTabView.Tab tab) {

            }

            @Override
            public void onTabReselected(CameraTabView.Tab tab) {

            }
        });
        mPreviewPresenter.setRecordSeconds(15);
    }

    /**
     * 显示影集蒙层
     */
    private void showVideoPicture() {
        // TODO 后续有时间再做
    }

    @Override
    public void onStart() {
        super.onStart();
        mPreviewPresenter.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        enhancementBrightness();
        mPreviewPresenter.onResume();
        Log.d(TAG, "onResume: ");
    }

    /**
     * 增强光照
     */
    private void enhancementBrightness() {
        BrightnessUtils.setWindowBrightness(mActivity, mCameraParam.luminousEnhancement
                ? BrightnessUtils.MAX_BRIGHTNESS : mCameraParam.brightness);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPreviewPresenter.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    public void onStop() {
        super.onStop();
        mPreviewPresenter.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    public void onDestroyView() {
        mContentView = null;
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: ");
    }

    @Override
    public void onDestroy() {
        destroyImageLoader();
        mPreviewPresenter.onDestroy();
        dismissDialog();
        mMainHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    public void onDetach() {
        mPreviewPresenter.onDetach();
        mPreviewPresenter = null;
        mActivity = null;
        super.onDetach();
        Log.d(TAG, "onDetach: ");
    }

    /**
     * 处理返回按钮事件
     * @return 是否拦截返回按键事件
     */
    public boolean onBackPressed() {
        Fragment fragment = getChildFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment != null) {
            hideFragmentAnimating();
            return true;
        }

        return false;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_stickers) {
            showStickers();
        }
    }

    /**
     * 销毁当前的对话框
     */
    private void dismissDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    /**
     * 关闭相机
     */
    private void closeCamera() {
        if (mActivity != null) {
            mActivity.finish();
            mActivity.overridePendingTransition(0, R.anim.anim_slide_down);
        }
    }

    /**
     * 切换相机
     */
    private void switchCamera() {
        if (!isCameraEnable()) {
            PermissionUtils.requestCameraPermission(this);
            return;
        }
        mPreviewPresenter.switchCamera();
    }

    /**
     * 是否显示速度条
     * @param show
     */
    private void setShowingSpeedBar(boolean show) {
        mSpeedBarShowing = show;
        mSpeedBar.setVisibility(show ? View.VISIBLE : View.GONE);
        mPreviewTopbar.setSpeedBarOpen(show);
    }

    /**
     * 显示设置页面
     */
    private void showSettingFragment() {
        if (mFragmentAnimating) {
            return;
        }
        if (mSettingFragment == null) {
            mSettingFragment = new PreviewSettingFragment();
        }
        mSettingFragment.addStateChangedListener(mStateChangedListener);
        mSettingFragment.setEnableChangeFlash(mCameraParam.supportFlash);
        if (!mSettingFragment.isAdded()) {
            getChildFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_bottom_container, mSettingFragment, FRAGMENT_TAG)
                    .addToBackStack(FRAGMENT_TAG)
                    .commitAllowingStateLoss();
        } else {
            getChildFragmentManager()
                    .beginTransaction()
                    .show(mSettingFragment)
                    .commitAllowingStateLoss();
        }
        showFragmentAnimating();
    }

    /**
     * 显示动态贴纸页面
     */
    private void showStickers() {
        if (mFragmentAnimating) {
            return;
        }
        if (mResourcesFragment == null) {
            mResourcesFragment = new PreviewResourceFragment();
        }
        mResourcesFragment.addOnChangeResourceListener((data) -> {
            mPreviewPresenter.changeResource(data);
        });
        if (!mResourcesFragment.isAdded()) {
            getChildFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_bottom_container, mResourcesFragment, FRAGMENT_TAG)
                    .commitAllowingStateLoss();
        } else {
            getChildFragmentManager()
                    .beginTransaction()
                    .show(mResourcesFragment)
                    .commitAllowingStateLoss();
        }
        showFragmentAnimating(false);
    }

    /**
     * 显示滤镜页面
     */
    private void showEffectFragment() {
        if (mFragmentAnimating) {
            return;
        }
        if (mEffectFragment == null) {
            mEffectFragment = new PreviewEffectFragment();
        }
        mEffectFragment.addOnCompareEffectListener(compare -> {
            mPreviewPresenter.showCompare(compare);
        });
        mEffectFragment.addOnFilterChangeListener(color -> {
            mPreviewPresenter.changeDynamicFilter(color);
        });
        mEffectFragment.addOnMakeupChangeListener(makeup -> {
            mPreviewPresenter.changeDynamicMakeup(makeup);
        });
        mEffectFragment.scrollToCurrentFilter(mPreviewPresenter.getFilterIndex());
        if (!mEffectFragment.isAdded()) {
            getChildFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_bottom_container, mEffectFragment, FRAGMENT_TAG)
                    .commitAllowingStateLoss();
        } else {
            getChildFragmentManager()
                    .beginTransaction()
                    .show(mEffectFragment)
                    .commitAllowingStateLoss();
        }
        showFragmentAnimating();
    }

    /**
     * 显示Fragment动画
     */
    private void showFragmentAnimating() {
        showFragmentAnimating(true);
    }

    /**
     * 显示Fragment动画
     */
    private void showFragmentAnimating(boolean hideAllLayout) {
        if (mFragmentAnimating) {
            return;
        }
        mFragmentAnimating = true;
        mFragmentContainer.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.preview_slide_up);
        mFragmentContainer.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mFragmentAnimating = false;
                if (hideAllLayout) {
                    hideAllLayout();
                } else {
                    hideWithoutSwitch();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 隐藏Fragment动画
     */
    private void hideFragmentAnimating() {
        if (mFragmentAnimating) {
            return;
        }
        mFragmentAnimating = true;
        Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.preivew_slide_down);
        mFragmentContainer.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                resetAllLayout();
                removeFragment();
                mFragmentAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 移除Fragment
     */
    private void removeFragment() {
        Fragment fragment = getChildFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment != null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .remove(fragment)
                    .commitAllowingStateLoss();
        }
    }

    /**
     * 隐藏所有布局
     */
    private void hideAllLayout() {
        mMainHandler.post(()-> {
            if (mPreviewTopbar != null) {
                mPreviewTopbar.hideAllView();
            }
            if (mSpeedBar != null) {
                mSpeedBar.setVisibility(View.GONE);
            }
            if (mBtnRecord != null) {
                mBtnRecord.setVisibility(View.GONE);
            }
            if (mLayoutMedia != null) {
                mLayoutMedia.setVisibility(View.GONE);
            }
            if (mLayoutDelete != null) {
                mLayoutDelete.setVisibility(View.GONE);
            }
            if (mCameraTabView != null) {
                mCameraTabView.setVisibility(View.GONE);
            }
            if (mTabIndicator != null) {
                mTabIndicator.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 隐藏除切换相机按钮外的所有控件
     */
    private void hideWithoutSwitch() {
        mMainHandler.post(() -> {
            if (mPreviewTopbar != null) {
                mPreviewTopbar.hideWithoutSwitch();
            }
            if (mSpeedBar != null) {
                mSpeedBar.setVisibility(View.GONE);
            }
            if (mBtnRecord != null) {
                mBtnRecord.setVisibility(View.GONE);
            }
            if (mLayoutMedia != null) {
                mLayoutMedia.setVisibility(View.GONE);
            }
            if (mLayoutDelete != null) {
                mLayoutDelete.setVisibility(View.GONE);
            }
            if (mCameraTabView != null) {
                mCameraTabView.setVisibility(View.GONE);
            }
            if (mTabIndicator != null) {
                mTabIndicator.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 录制状态隐藏
     */
    public void hideOnRecording() {
        mMainHandler.post(()-> {
            if (mPreviewTopbar != null) {
                mPreviewTopbar.hideAllView();
            }
            if (mSpeedBar != null) {
                mSpeedBar.setVisibility(View.GONE);
            }
            if (mLayoutMedia != null) {
                mLayoutMedia.setVisibility(View.GONE);
            }
            if (mCameraTabView != null) {
                mCameraTabView.setVisibility(View.GONE);
            }
            if (mTabIndicator != null) {
                mTabIndicator.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 恢复所有布局
     */
    public void resetAllLayout() {
        mMainHandler.post(()-> {
            if (mPreviewTopbar != null) {
                mPreviewTopbar.resetAllView();
            }
            setShowingSpeedBar(mSpeedBarShowing);
            if (mBtnRecord != null) {
                mBtnRecord.setVisibility(View.VISIBLE);
            }
            if (mLayoutDelete != null) {
                mLayoutDelete.setVisibility(View.VISIBLE);
            }
            if (mCameraTabView != null) {
                mCameraTabView.setVisibility(View.VISIBLE);
            }
            if (mTabIndicator != null) {
                mTabIndicator.setVisibility(View.VISIBLE);
            }
            resetDeleteButton();
            if (mBtnRecord != null) {
                mBtnRecord.reset();
            }
        });
    }

    /**
     * 复位删除按钮
     */
    private void resetDeleteButton() {
        boolean hasRecordVideo = (mPreviewPresenter.getRecordedVideoSize() > 0);
        if (mLayoutMedia != null) {
            mLayoutMedia.setVisibility(hasRecordVideo ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * 拍照
     */
    private void takePicture() {
        if (isStorageEnable()) {
            if (mCameraParam.mGalleryType == GalleryType.PICTURE) {
                if (mCameraParam.takeDelay) {
                    //mCountDownView.start();
                    hideAllLayout();
                } else {
                    mPreviewPresenter.takePicture();
                }
            }
        } else {
            PermissionUtils.requestStoragePermission(this);
        }
    }



    // ---------------------------- TextureView SurfaceTexture监听 ---------------------------------
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mPreviewPresenter.onSurfaceCreated(surface);
            mPreviewPresenter.onSurfaceChanged(width, height);
            Log.d(TAG, "onSurfaceTextureAvailable: ");
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            mPreviewPresenter.onSurfaceChanged(width, height);
            Log.d(TAG, "onSurfaceTextureSizeChanged: ");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            mPreviewPresenter.onSurfaceDestroyed();
            Log.d(TAG, "onSurfaceTextureDestroyed: ");
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    // ----------------------------------- 顶部状态栏点击回调 ------------------------------------
    private PreviewSettingFragment.StateChangedListener mStateChangedListener = new PreviewSettingFragment.StateChangedListener() {

        @Override
        public void flashStateChanged(boolean flashOn) {
            // todo 闪光灯切换

        }

        @Override
        public void onOpenCameraSetting() {
            mPreviewPresenter.onOpenCameraSettingPage();
        }

        @Override
        public void delayTakenChanged(boolean enable) {
            mCameraParam.takeDelay = enable;
        }

        @Override
        public void luminousCompensationChanged(boolean enable) {
            mCameraParam.luminousEnhancement = enable;
            enhancementBrightness();
        }

        @Override
        public void touchTakenChanged(boolean touchTake) {
            mCameraParam.touchTake = touchTake;
        }

        @Override
        public void changeEdgeBlur(boolean enable) {
            mPreviewPresenter.enableEdgeBlurFilter(enable);
        }
    };

    /**
     * 显示fps
     * @param fps
     */
    public void showFps(final float fps) {
        mMainHandler.post(() -> {
            if (mCameraParam.showFps) {
                mFpsView.setText("fps = " + fps);
                mFpsView.setVisibility(View.VISIBLE);
            } else {
                mFpsView.setVisibility(View.GONE);
            }
        });
    }


    private Toast mToast;
    /**
     * 显示Toast提示
     * @param msg
     */
    public void showToast(String msg) {
        mMainHandler.post(() -> {
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT);
            mToast.show();
        });
    }

    // -------------------------------------- 权限逻辑处理 ---------------------------------
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionUtils.REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                PermissionErrorDialogFragment.newInstance(getString(R.string.request_camera_permission), PermissionUtils.REQUEST_CAMERA_PERMISSION, true)
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            } else {
                initView(mContentView);
            }
        } else if (requestCode == PermissionUtils.REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                PermissionErrorDialogFragment.newInstance(getString(R.string.request_storage_permission), PermissionUtils.REQUEST_STORAGE_PERMISSION)
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            }
        } else if (requestCode == PermissionUtils.REQUEST_SOUND_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                PermissionErrorDialogFragment.newInstance(getString(R.string.request_sound_permission), PermissionUtils.REQUEST_SOUND_PERMISSION)
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * 是否允许拍摄
     * @return
     */
    private boolean isCameraEnable() {
        return PermissionUtils.permissionChecking(mActivity, Manifest.permission.CAMERA);
    }

    /**
     * 判断是否可以读取本地媒体
     * @return
     */
    private boolean isStorageEnable() {
        return PermissionUtils.permissionChecking(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    // -------------------------------------- 缩略图加载逻辑 start ---------------------------------
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle bundle) {
        return AlbumDataLoader.getImageLoaderWithoutBucketSort(mActivity);
    }


    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    /**
     * 销毁加载器
     */
    private void destroyImageLoader() {
        if (mLocalImageLoader != null) {
            mLocalImageLoader.destroyLoader(ALBUM_LOADER_ID);
            mLocalImageLoader = null;
        }
    }
    // -------------------------------------- 缩略图加载逻辑 end -----------------------------------
}
