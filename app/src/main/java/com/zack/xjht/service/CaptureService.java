package com.zack.xjht.service;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.alibaba.fastjson.JSONException;
import com.androidhiddencamera.CameraConfig;
import com.androidhiddencamera.CameraError;
import com.androidhiddencamera.HiddenCameraService;
import com.androidhiddencamera.HiddenCameraUtils;
import com.androidhiddencamera.config.CameraFacing;
import com.androidhiddencamera.config.CameraImageFormat;
import com.androidhiddencamera.config.CameraResolution;
import com.yanzhenjie.nohttp.rest.Response;
import com.zack.xjht.Constants;
import com.zack.xjht.R;
import com.zack.xjht.Utils.CloseUtil;
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.Utils.ToastUtil;
import com.zack.xjht.Utils.Utils;
import com.zack.xjht.http.HttpClient;
import com.zack.xjht.http.HttpListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 抓拍服务
 */
public class CaptureService extends HiddenCameraService {
    private static final String TAG = "CaptureService3";
    private CameraConfig cameraConfig;

    public CaptureService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand: ");
        takeImage();
        return START_NOT_STICKY;
    }

    private void takeImage() {
        if (ActivityCompat.checkSelfPermission(CaptureService.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) { //有相机权限
            //Api小于23返回true
            if (HiddenCameraUtils.canOverDrawOtherApps(CaptureService.this)) {
                Log.i(TAG, "onStartCommand canOverDrawOtherApps: ");
                cameraConfig = new CameraConfig()
                        .getBuilder(CaptureService.this)
                        .setCameraFacing(CameraFacing.REAR_FACING_CAMERA)
                        .setCameraResolution(CameraResolution.MEDIUM_RESOLUTION)
                        .setImageFormat(CameraImageFormat.FORMAT_JPEG)
                        .build();
                //启动相机
                startCamera(cameraConfig);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "run 正在抓拍Thread: " + Thread.currentThread().getId());
                        Constants.isCapturing =true;//设置为正在抓拍
                        //抓拍
                        takePicture();
                    }
                }, 0);
            } else {
                Log.w(TAG, " Open settings to grant permission for \"Draw other apps\".: ");
                HiddenCameraUtils.openDrawOverPermissionSetting(CaptureService.this);
            }
        } else { //没有相机权限
            ToastUtil.showShort(R.string.permission_not_available);
            Log.i(TAG, "takeImage 没有相机权限: ");
        }
    }

    @Override
    public void onImageCapture(@NonNull final File imageFile) {
        Log.i(TAG, "onImageCapture: "+Thread.currentThread().getId());
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = null;
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(imageFile);
                    int available = fis.available();
                    byte[] data = new byte[available];
                    fis.read(data);
                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    Log.i(TAG, "run 获取到bitmap: ");
                    if (bitmap != null) {
                        String base64Str = Utils.bitmapToBase64Str(bitmap);
                        Log.i(TAG, "run bitmap length: "+base64Str.length());
                        postCapturePic(base64Str);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    CloseUtil.closeQuietly(fis);
                }
            }
        }).start();
        Constants.isCapturing =false;
        stopSelf();
    }

    private void postCapturePic(String base64String) {
        HttpClient.getInstance().postCapturePhoto(CaptureService.this, base64String,
                new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "postCapturePic onSucceed  response: " + response.get());
                SharedUtils.setIsCapturing(false);
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                Log.i(TAG, "onFailed error: " + response.getException().getMessage());
            }
        });
    }

    @Override
    public void onCameraError(int errorCode) {
        Log.i(TAG, "onCameraError: ");
        switch (errorCode) {
            case CameraError.ERROR_CAMERA_OPEN_FAILED:
                //Camera open failed. Probably because another application
                //is using the camera
                ToastUtil.showShort(R.string.can_not_open_camera);
                break;
            case CameraError.ERROR_IMAGE_WRITE_FAILED:
                //Image write failed. Please check if you have provided WRITE_EXTERNAL_STORAGE permission
                ToastUtil.showShort(R.string.cannot_write_image_captured);
                break;
            case CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE:
                //camera permission is not available
                //Ask for the camera permission before initializing it.
                ToastUtil.showShort(R.string.permission_not_available);
                break;
            case CameraError.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION:
                //Display information dialog to the user with steps to grant "Draw over other app"
                //permission for the app.
                HiddenCameraUtils.openDrawOverPermissionSetting(this);
                break;
            case CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA:
                ToastUtil.showShort(R.string.no_front_camera);
                break;
        }
//        SharedUtils.setIsCapturing(false);
        Constants.isCapturing =false;
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy 结束抓拍: ");
//        SharedUtils.setIsCapturing(false);
        Constants.isCapturing =false;
    }
}
