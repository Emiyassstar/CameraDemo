package com.example.emiyasstar.camerademo;

import android.annotation.SuppressLint;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.lang.reflect.Method;

import cn.redcdn.log.CustomLog;
import cn.redcdn.log.LogcatFileManager;

public class MainActivity extends AppCompatActivity {

    private static String TAG="MainActivity";
    SurfaceView cameraPreview;

    private static Method addCallbackBuffer = null;
    private static Method setPreviewCallbackWithBuffer = null;

    private Camera camera = null;

    private int vwidth =320;
    private int vheight = 320;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogcatFileManager.getInstance().setLogDir("com.example.camerademo");
        LogcatFileManager.getInstance().start("camerademo");
        CustomLog.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_main);
        cameraPreview=(SurfaceView) findViewById(R.id.local_video_surfaceview);

        try {
            if (addCallbackBuffer == null)
                addCallbackBuffer = Camera.class.getMethod("addCallbackBuffer",
                        byte[].class);
            if (addCallbackBuffer == null) {
                CustomLog.d(TAG, "method addCallbackBuffer no found!");
            }
            if (setPreviewCallbackWithBuffer == null)
                setPreviewCallbackWithBuffer = Camera.class.getMethod(
                        "setPreviewCallbackWithBuffer", Camera.PreviewCallback.class);
            if (setPreviewCallbackWithBuffer == null) {
                CustomLog.d(TAG,
                        "method setPreviewCallbackWithBuffer no found!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        ViewGroup.LayoutParams params = cameraPreview.getLayoutParams();
//        vheight = params.height;
//        vwidth = params.width;

        DisplayMetrics dm = new DisplayMetrics();
        dm = this.getResources().getDisplayMetrics();
        int screen_w = dm.widthPixels;
        int screen_h = dm.heightPixels;
        CustomLog.d(TAG,"screenPixels: "+screen_w+" "+screen_h);
        cameraPreview.getHolder().setType(
                SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        cameraPreview.getHolder().setFixedSize(vheight, vheight);
        cameraPreview.getHolder().addCallback(new CameraSurfaceCallback());
    }

    @Override
    protected void onResume() {
        super.onResume();
        CustomLog.d(TAG, "onResume: ");
//        setCameraDisplayOrientation();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    class CameraSurfaceCallback implements SurfaceHolder.Callback {
        // @Override
        public void surfaceChanged(SurfaceHolder holder, int fmt, int w, int h) {
            CustomLog.d(TAG, "camera surface changed.");
            if (camera != null) {
                try {
                    Camera.Parameters parameters = camera.getParameters();
                    ViewGroup.LayoutParams params = cameraPreview.getLayoutParams();
                    CustomLog.d(TAG,"cameraPreview params:"+params.width+";"+params.height);
                    parameters.setPreviewSize(vwidth,
                           vheight);
                    CustomLog.d(TAG, "camera.surfaceChanged width[" + w
                            + "]height[" + h + "]");
                    camera.setParameters(parameters);
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        }

        // @Override
        public void surfaceCreated(SurfaceHolder holder) {
            CustomLog.d(TAG, "camser surfaceCreated open camera.");
            int cameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
            startCamera_(cameraFacing, holder);
            setCameraDisplayOrientation();
        }

        // @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            CustomLog.d(TAG, "camera surface destroy, close camera.");
            stopCamera_();
        }
    }

    class CameraPreviewCallback implements Camera.PreviewCallback {
        // @Override
        public void onPreviewFrame(byte[] data, Camera camera) {

            addCallbackBuffer(camera, data);
        }
    }

    private static byte[] camera_buf_1 = null;

    private static byte[] camera_buf_2 = null;

    private static byte[] camera_buf_3 = null;

    void releaseCallbackBuffer() {
        camera_buf_1 = null;
        camera_buf_2 = null;
        camera_buf_3 = null;
    }

    void addCallbackBuffer(Camera camera, int w, int h) {
        CustomLog.d(TAG, "addCallbackBuffer width*height:[" + w + "*" + h
                + "].");
        int nLen = w * h * 3 / 2;
        camera_buf_1 = new byte[nLen];
        camera_buf_2 = new byte[nLen];
        camera_buf_3 = new byte[nLen];
        addCallbackBuffer(camera, camera_buf_1);
        addCallbackBuffer(camera, camera_buf_2);
        addCallbackBuffer(camera, camera_buf_3);
    }

    void addCallbackBuffer(Camera camera, byte[] buffer) {
        try {
            if (addCallbackBuffer != null)
                addCallbackBuffer.invoke(camera, buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private int cameraType = 1; // 鎽勫儚澶寸被鍨�

    /**
     * @Title: getCameraType
     * @Description: 获取摄像头类型(前置类型或后置类型)
     * @return: int
     */
    public int getCameraType() {
        return cameraType;
    }

    // 鍏抽棴褰撳墠鎽勫儚澶�
    public void stopCamera_() {
        // ManageLog.D(TAG, "stopCamera !");
        if (camera == null) {
            return;
        }

        CustomLog.d(TAG, "stopCamera [" + camera + "].");
        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        camera = null;
        releaseCallbackBuffer();
    }

    @SuppressLint("NewApi")
    public void startCamera_(int cameraFace, SurfaceHolder holder) {
        if ((Camera.CameraInfo.CAMERA_FACING_BACK != cameraFace)
                && (Camera.CameraInfo.CAMERA_FACING_FRONT != cameraFace)) {
            cameraFace = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }

        CustomLog.d(TAG, "startCamera_ Facing [" + cameraFace + "], holder ["
                + holder + "].");

        if (camera != null) {
            CustomLog.d(TAG, "startCamera_ camera!=null && return");
            return;
        }

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras(); // get cameras number
        CustomLog.d(TAG, "cameraCount [" + cameraCount + "]");
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            CustomLog.d(TAG, "cameraIdx [" + cameraInfo.facing + "]");
            try {
                if (cameraInfo.facing == cameraFace) {
                    camera = Camera.open(camIdx);
                    if (camera != null) {
                        cameraType = cameraFace;
                        break;
                    } else {
                        cameraType = -1;
                    }
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
                cameraType = -1;
                return;
            }
        }

        if (camera == null) {
            camera = Camera.open();
            if (camera != null) {
                cameraType = Camera.CameraInfo.CAMERA_FACING_BACK;
            } else {
                cameraType = -1;
            }
        }

        // Set Camera facing and local camera status
        if (camera == null) {
            // SipService.sendMessage(0,
            // EventConstant.open_camera_failed_notify, 0, "");
            cameraType = -1;
            return;
        } else {
            if (cameraType == Camera.CameraInfo.CAMERA_FACING_BACK) {
             //   enableUseBackCammera(true);
            }
            if (cameraType == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            //    enableUseBackCammera(false);
            }
        }

        // Set camera param
        try {
            Camera.Parameters parameters = camera.getParameters();
          //  ViewGroup.LayoutParams params = cameraPreview.getLayoutParams();
            parameters.setPreviewFormat(PixelFormat.YCbCr_420_SP);
            parameters.setPreviewFrameRate(15);
            parameters.setPreviewSize(vwidth, vheight);
            camera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // start camera
        try {

            if (setPreviewCallbackWithBuffer != null) {
                setPreviewCallbackWithBuffer.invoke(camera,
                        new CameraPreviewCallback());
                addCallbackBuffer(camera, vwidth, vheight);
            } else {
                camera.setPreviewCallback(new CameraPreviewCallback());
            }

            if (holder != null) {
                camera.setPreviewDisplay(holder);
            } else if (cameraPreview != null) {
                camera.setPreviewDisplay(cameraPreview.getHolder());
                cameraPreview.requestLayout();
            } else {

                cameraType = -1;
                return;
            }
            // start
            camera.startPreview();
        //    getService().addViewStatus(STARTED_PREVIEW);
        } catch (Exception exception) {
            camera.release();
            camera = null;
            cameraType = -1;
        }

    }


    // For Mobile: switch camera front/back.
    public void switchCamera(int type) {

        if (camera == null || cameraType == -1) {
            return;
        } else {
            stopCamera_();
        }

        int camera_type_new = -1;

        if (cameraType == Camera.CameraInfo.CAMERA_FACING_FRONT) { // conver
            // front to
            // back
            camera_type_new = Camera.CameraInfo.CAMERA_FACING_BACK;
            CustomLog.d(TAG, "switch camera to CAMERA_FACING_BACK!");
        } else { // back to front or default front
            camera_type_new = Camera.CameraInfo.CAMERA_FACING_FRONT;
            CustomLog.d(TAG, "switch camera to CAMERA_FACING_FRONT!");
        }
        if (-1 == camera_type_new) {
            // default FRONT camera
            camera_type_new = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        //sendBroadcase("BUTEL_SWIHCH_CAMERA", "null");
        // Start New camera
        cameraType = camera_type_new;
        startCamera_(camera_type_new, null);
    }


    void tryRestartCamera(int reason) {
        CustomLog.d(TAG, "tryCloseCamera width*height:[" + vwidth + "*"
                + vheight + "].");
        if (camera == null) {
            CustomLog.d(TAG, "tryCloseCamera camera==null && return");
            return;
        }

        stopCamera_();
        if (reason == 0) {
            // reason 0, close camera
    //        isVideoMute = true; // Set video mute
            if (cameraPreview != null)
                cameraPreview.setVisibility(View.INVISIBLE);
            return;
        }
        // restart current camera
        startCamera_(cameraType, null);
    }

    /**
     * 设置摄像头角度
     */
    @SuppressLint("NewApi")
    public void setCameraDisplayOrientation() {
        int cameraId = cameraType;
       // Camera camera = camera;
        if (camera == null) {
            return;
        }
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        if (cameraId < 0 || cameraId >= Camera.getNumberOfCameras()) {
            cameraId = 0;
        }
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = this.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        CustomLog.d(TAG,"setCameraDisplayOrientation()degrees = " + degrees);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        CustomLog.d(TAG,"setCameraDisplayOrientation()result = " + result);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//            if(isBaiduTest&&isPortrait()){
//                result=0;
//            }
            camera.setDisplayOrientation(result);
        }
    }


}
