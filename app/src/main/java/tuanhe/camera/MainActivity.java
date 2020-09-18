package tuanhe.camera;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import java.net.Socket;
import java.util.Date;

import static tuanhe.camera.CameraView.timestamp_trig;


@RequiresApi(api = Build.VERSION_CODES.P)
public class MainActivity extends AppCompatActivity {
    private static String TAG;
    private static CameraView cv;
    private static SurfaceHolder sh;
    public static Button btnStart,btnRecord,btnTrig;
    public static TextView tipstext;
    public static TextView textMessage;
    public static SettingsDialog sv;
    public static WifiManager wifiManager = null;
    public static Socket trigsocket = null;
    public static Context global_context;
    public static boolean isconnected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        global_context = this;

        sv = new SettingsDialog(this);
        setContentView(R.layout.activity_main);

        wifiManager = (WifiManager)getApplicationContext().getSystemService(this.WIFI_SERVICE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        btnRecord = findViewById(R.id.btn_record);
        btnStart = findViewById(R.id.btn_start);
        btnTrig = findViewById(R.id.btn_trig);

        cv = findViewById(R.id.cameraView);

        sv.hide();

        tipstext = findViewById(R.id.clicktext);
        textMessage = findViewById(R.id.textMessage);

        sh = cv.getHolder();

        sh.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                sh.setFixedSize(1280,720);
                cv.initCamera();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sv.show();
            }
        });

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (cv.onRecording()){
                    case -1:
                        break;
                    case 1:
                        Animation stoprecord = AnimationUtils.loadAnimation(view.getContext(),
                                R.anim.stoprecord);
                        btnRecord.startAnimation(stoprecord);
                        btnRecord.setBackgroundResource(R.drawable.stop);
                        break;
                    case 0:
                        btnRecord.setBackgroundResource(R.drawable.record);
                        btnStart.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        btnTrig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation triganim = AnimationUtils.loadAnimation(view.getContext(),
                        R.anim.trig);
                btnTrig.startAnimation(triganim);
                if(wifiManager.getConnectionInfo().getSSID().equals("\"Cubie\"") && MainActivity.isconnected){
                    new starttrig().start();
                    timestamp_trig = new Date().getTime();
                }
                else
                    new startwireless().start();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        new startwireless().start();
        Log.d(TAG, "onStart: ");
    }



}
