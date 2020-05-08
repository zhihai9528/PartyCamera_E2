package library.cameralibrary.camera;

import android.graphics.SurfaceTexture;

public interface OnFrameAvailableListener {
    void onFrameAvailable(SurfaceTexture surfaceTexture);
}
