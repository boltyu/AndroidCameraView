package tuanhe.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.gesture.Gesture;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.MotionEventCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;

@RequiresApi(api = Build.VERSION_CODES.P)
public class CameraView extends SurfaceView {

    private String TAG = "";
    private static SurfaceHolder sh = null;
    private Context sContext = null;
    private CameraDevice cd = null;
    private CameraCaptureSession previewSession = null;
    private boolean isRecording = false;
    public GestureDetectorCompat gestureDetector;
    private static MediaRecorder mMediaRecorder;
    public static String s_timestamp_record = "";
    public static long timestamp_trig = 0;
    private String filefullpath = "";

    private CameraDevice.StateCallback cameraCallback = new CameraDevice.StateCallback() {
        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            Log.d(TAG, "Camera 0 opened");
           cd = cameraDevice;
           startPreview();
        }
        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            Log.d(TAG, "Camera 0 disconnected");
        }
        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            Log.d(TAG, "Camera 0 encounter error");
        }
    };

    public CameraView(Context context) {
        super(context);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sContext = context;
        sh = this.getHolder();
        gestureDetector = new GestureDetectorCompat(context, new MyGestureListener());
        mMediaRecorder = new MediaRecorder();
        initCamera();
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public int initCamera(){
        CameraManager cameraManager = (CameraManager) sContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIdList = cameraManager.getCameraIdList();
            for (String i: cameraIdList) {
                Log.d(TAG,"Camera " + i + " Found");
            }
            Log.d(TAG, "Choosing Camera "+ cameraIdList[0]+"\n get characteristics");
            CameraCharacteristics ccs = cameraManager.getCameraCharacteristics(cameraIdList[0]);
            StreamConfigurationMap scm = ccs.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] supportsize = scm.getOutputSizes(ImageFormat.JPEG);
            for (Size i:supportsize) {
                Log.d(TAG, "JPEG support size: "+ String.valueOf(i.getWidth()) + "*" +String.valueOf(i.getHeight()));
            }
            cameraManager.openCamera(cameraIdList[0], cameraCallback, null);
        } catch (CameraAccessException | SecurityException e) {
            Log.e(TAG, e.toString());
        }
        return 0;
    }

    public int onRecording(){
        if(isRecording) {
            stopRecord();
            isRecording = false;
            MainActivity.tipstext.setText("");
            File filefrom = new File(filefullpath);
            File fileto = new File(filefullpath + "+SYNC" + String.valueOf(timestamp_trig)+ ".mp4");
            filefrom.renameTo(fileto);
        }else{
            if(startRecord() == 0){
                timestamp_trig = new Date().getTime();
                MainActivity.tipstext.setText(R.string.recoding);
                s_timestamp_record = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
                isRecording = true;
            }else {
                MainActivity.tipstext.setText("Video path error");
            }
        }
        return 0;
    }

    private int startRecord(){
        closePreviewSession();
        if(setUpMediaRecorder() == 0) {
            try {
                final CaptureRequest.Builder prb = cd.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                prb.addTarget(sh.getSurface());
                prb.addTarget(mMediaRecorder.getSurface());
                CameraCaptureSession.StateCallback ccssc = new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                        Log.d(TAG, "Configured: ");
                        previewSession = cameraCaptureSession;
                        CaptureRequest previewRequest = prb.build();
                        try {
                            previewSession.setRepeatingRequest(previewRequest, null, null);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                        mMediaRecorder.start();
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                        Log.d(TAG, "onConfigureFailed: ");
                    }
                };
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    SessionConfiguration sessionConfiguration = new SessionConfiguration(SessionConfiguration.SESSION_REGULAR
                            , Collections.singletonList(new OutputConfiguration(sh.getSurface()))
                            , new Executor() {
                        @Override
                        public void execute(Runnable runnable) {
                            Log.d(TAG, "execute");
                            runnable.run();
                        }
                    }, ccssc);
                    cd.createCaptureSession(sessionConfiguration);
                } else {
                    cd.createCaptureSession(Arrays.asList(sh.getSurface(), mMediaRecorder.getSurface()), ccssc, null);
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
                return -1;
            }
            return 0;
        }
        else
            return -1;
    }

    private void stopRecord(){
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        startPreview();
    }

    private void startPreview(){
        try {
            final CaptureRequest.Builder prb = cd.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            prb.addTarget(sh.getSurface());
            prb.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            prb.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            CameraCaptureSession.StateCallback ccssc = new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.d(TAG, "Configured: ");
                    try {
                        previewSession = cameraCaptureSession;
                        CaptureRequest previewRequest = prb.build();
                        previewSession.setRepeatingRequest(previewRequest, null, null);
                    } catch (CameraAccessException a) {
                        Log.e(TAG, "onConfigured error ");
                    }
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.d(TAG, "onConfigureFailed: ");
                }
            };
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                SessionConfiguration sessionConfiguration = new SessionConfiguration(SessionConfiguration.SESSION_REGULAR
                        , Collections.singletonList(new OutputConfiguration(sh.getSurface()))
                        , new Executor() {
                    @Override
                    public void execute(Runnable runnable) {
                        Log.d(TAG, "execute");
                        runnable.run();
                    }
                }, ccssc);
                cd.createCaptureSession(sessionConfiguration);
            } else {
                cd.createCaptureSession(Collections.singletonList(sh.getSurface()), ccssc, null);
            }
        }catch (CameraAccessException cae){
            Log.e(TAG, "cae");
        }
        return;

    }

    private void closePreviewSession() {
        if (previewSession != null) {
            previewSession.close();
            previewSession = null;
        }
    }

    private int setUpMediaRecorder() {
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        // Step 4: Set output file
        //mMediaRecorder.setOutputFile(Environment.getExternalStorageDirectory() + File.separator + "A.mp4");
        filefullpath = MainActivity.sv.GetFilePath() + File.separator + MainActivity.sv.GetFileName() + "_" + s_timestamp_record;
        mMediaRecorder.setOutputFile(filefullpath);
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(1280,960);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(sh.getSurface());
        mMediaRecorder.setOrientationHint(90);
        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return -1;
        }
        return 0;
    }

    public static void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;        // lock camera for later use
            //releaseCamera();
        }
    }

    public boolean IsRecording(){
        return isRecording;
    }

    // Gesture Processing
    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final String DEBUG_TAG = "Gestures";

        @Override
        public boolean onDown(MotionEvent event) {
            Log.d(DEBUG_TAG,"onDown: " + event.toString());
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            Log.d(DEBUG_TAG, "onFling: " + event1.toString() + event2.toString());
            if((event2.getY() - event1.getY()) > 200){
                // swipe up to trigger
                MainActivity.sv.hide();

            }else if((event1.getY() - event2.getY()) > 200){
                // swipe down to edit info
                MainActivity.sv.show();
            }
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e){
            onRecording();
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e){
            // LongPress to start recording
            if(MainActivity.wifiManager.getConnectionInfo().getSSID().equals("\"Cubie\"") && MainActivity.isconnected){
                new starttrig().start();
                timestamp_trig = new Date().getTime() - timestamp_trig;
                if(timestamp_trig < 0)
                    timestamp_trig = 0;
            }
            else
                new startwireless().start();
        }
    }
}
