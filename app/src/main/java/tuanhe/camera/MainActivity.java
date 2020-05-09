package tuanhe.camera;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.widget.TextView;

import java.net.Socket;


@RequiresApi(api = Build.VERSION_CODES.P)
public class MainActivity extends AppCompatActivity {
    private static String TAG;
    private static CameraView cv;
    private static SurfaceHolder sh;
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

        cv = findViewById(R.id.cameraView);

        sv.hide();

        tipstext = findViewById(R.id.clicktext);
        textMessage = findViewById(R.id.textMessage);

        sh = cv.getHolder();
        sh.setFixedSize(640,480);

        new startwireless().start();
    }

    // Gesture Processing
    @Override
    public boolean onTouchEvent(MotionEvent event){
        // forward to CameraView's listener
        cv.gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }


}
