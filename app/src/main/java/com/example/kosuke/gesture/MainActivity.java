package com.example.kosuke.gesture;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSION = 200;

    private Camera mCamera;
    private TextureView mTextureView;

    private Timer mTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || PermissionChecker.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(this, PermissionActivity.class);
            startActivityForResult(intent, REQUEST_PERMISSION);
        }

        mTextureView = findViewById(R.id.texture);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mCamera.open();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCamera = new Camera(this, mTextureView);
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new CaptureTask(this, mCamera), 0, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mCamera != null) mCamera.close();
        if (mTimer != null) mTimer.cancel();
        mTimer = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_PERMISSION) {
            if (resultCode == RESULT_CANCELED) finish();
        }
    }
}


class Camera {
    private static final String TAG = Camera.class.getSimpleName();

    private CameraDevice mCameraDevice;
    private TextureView mTextureView;
    private Size mCameraSize;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mPreviewSession;
    private Context mContext;

    private CameraDevice.StateCallback mCameraDeviceCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            createCaptureSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
        }
    };

    private CameraCaptureSession.StateCallback mCameraCaptureSessionCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            mPreviewSession = session;
            updatePreview();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Toast.makeText(mContext, "onConfigureFailed", Toast.LENGTH_LONG).show();
        }
    };

    public Camera(Context context, TextureView textureView) {
        this.mContext = context;
        this.mTextureView = textureView;
    }

    public void open() {
        try {
            CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    mCameraSize = map.getOutputSizes(SurfaceTexture.class)[0];
                    if (PermissionChecker.checkSelfPermission(mContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        manager.openCamera(cameraId, mCameraDeviceCallback, null);
                    }
                    return;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCaptureSession() {
        if (!mTextureView.isAvailable()) {
            return;
        }

        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        texture.setDefaultBufferSize(mCameraSize.getWidth(), mCameraSize.getHeight());
        Surface surface = new Surface(texture);
        try {
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        mPreviewBuilder.addTarget(surface);
        try {
            mCameraDevice.createCaptureSession(Collections.singletonList(surface), mCameraCaptureSessionCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        HandlerThread thread = new HandlerThread("CameraPreview");
        thread.start();
        Handler backgroundHandler = new Handler(thread.getLooper());

        try {
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public Bitmap capture() {
        if (!mTextureView.isAvailable()) return null;
        Log.d(TAG, "Capture.");
        Bitmap bitmap;
        bitmap = mTextureView.getBitmap();
        return bitmap;
    }

    public void close() {
        if (mPreviewSession != null) mPreviewSession.close();
        if (mCameraDevice != null) mCameraDevice.close();
    }

    public void save(Bitmap bitmap) {
        if (PermissionChecker.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            MediaStore.Images.Media.insertImage(
                    mContext.getContentResolver(),
                    bitmap,
                    null,
                    null
            );
        }
    }
}

class CaptureTask extends TimerTask {
    private static final String TAG = CaptureTask.class.getSimpleName();

    private Context mContext;
    private Camera mCamera;

    public CaptureTask(Context context, Camera camera) {
        this.mContext = context;
        this.mCamera = camera;
    }

    @Override
    public void run() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = mCamera.capture();
                if (bitmap == null) {
                    Log.d(TAG, "Terminate Capture task due to bitmap is null.");
                    return;
                }

                HashMap<String, String> params = new HashMap<>();

                params.put("api_key", mContext.getString(R.string.api_key));
                params.put("api_secret", mContext.getString(R.string.api_secret_key));
                params.put("image_base64", convert2Base64(bitmap));

                AsyncHttp task = new AsyncHttp();
                task.setOnCallBack(new AsyncHttp.CallBackTask() {
                    @Override
                    public void CallBack(JSONObject json) {
                        if (json == null) return;
                        Log.d(TAG, "Responce body: " + json.toString());
                    }
                });
                task.execute(params);
                mCamera.save(bitmap);
            }
        }).start();
    }

    public static String convert2Base64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
    }
}
