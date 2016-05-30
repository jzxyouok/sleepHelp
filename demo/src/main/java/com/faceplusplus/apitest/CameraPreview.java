
package com.faceplusplus.apitest;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.faceplusplus.api.FaceDetecter;
import com.faceplusplus.api.FaceDetecter.Face;
import com.megvii.apitest.R;

import java.io.IOException;

public class CameraPreview extends Activity implements Callback, PreviewCallback {
    SurfaceView camerasurface = null;
    FaceMask mask = null;
    Camera camera = null;
    HandlerThread handleThread = null;
    Handler detectHandler = null;
    Runnable detectRunnalbe = null;
    private int width = 320;
    private int height = 240;
    FaceDetecter facedetecter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camerapreview);
        camerasurface = (SurfaceView) findViewById(R.id.camera_preview);
        mask = (FaceMask) findViewById(R.id.mask);
        LayoutParams para = new LayoutParams(480, 640); //新建一个布局对象
        handleThread = new HandlerThread("dt");
        handleThread.start();
        detectHandler = new Handler(handleThread.getLooper());

        para.addRule(RelativeLayout.CENTER_IN_PARENT);//设置布局对象的位置属性
        camerasurface.setLayoutParams(para);//把布局对象作为参数传入到surfaceview里面

        mask.setLayoutParams(para);

        camerasurface.getHolder().addCallback(this);//为surfaceholder添加回调接口
        camerasurface.setKeepScreenOn(true);//设置屏幕一直是唤醒状态

        facedetecter = new FaceDetecter();
        if (!facedetecter.init(this, "35a467be6126eda75a31818ddd9e483e")) {
            Log.e("diff", "有错误 ");
        }
        facedetecter.setTrackingMode(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera = Camera.open(1);
        Camera.Parameters para = camera.getParameters();
        para.setPreviewSize(width, height);
        camera.setParameters(para);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        facedetecter.release(this);
        handleThread.quit();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.setDisplayOrientation(90);
        camera.startPreview();
        camera.setPreviewCallback(this);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onPreviewFrame(final byte[] data, Camera camera) {
        camera.setPreviewCallback(null);
        detectHandler.post(new Runnable() {

            @Override
            public void run() {
                byte[] ori = new byte[width * height];
                int is = 0;
                for (int x = width - 1; x >= 0; x--) {

                    for (int y = height - 1; y >= 0; y--) {

                        ori[is] = data[y * width + x];

                        is++;
                    }

                }
                final Face[] faceinfo = facedetecter.findFaces( ori, height,
                        width);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mask.setFaceInfo(faceinfo);
                    }
                });
                CameraPreview.this.camera.setPreviewCallback(CameraPreview.this);
            }
        });
    }
}