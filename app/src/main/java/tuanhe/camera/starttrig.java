package tuanhe.camera;

import android.os.Build;
import android.view.View;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

class starttrig extends Thread{
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void run(){
        OutputStream trigsend = null;
        try {
            if(MainActivity.trigsocket == null) {
                MainActivity.trigsocket = new Socket("192.168.4.1", 11901);
                if (MainActivity.trigsocket != null) {
                    trigsend = MainActivity.trigsocket.getOutputStream();
                    byte trigcmd[] = {0x01, 0x02, 0x2c, 0x01, 0x04, 0x00, 0x00, 0x00, 0x7e, 0x11, 0x01, 0x00};
                    trigsend.write(trigcmd);
                    trigsend.close();
                }
                MainActivity.trigsocket.close();
                MainActivity.trigsocket = null;
            }
        } catch (IOException e) {//timeout
            MainActivity.textMessage.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.textMessage.setText(MainActivity.sv.GetFileName() + "\n模块连接可能丢失");
                }
            });
            MainActivity.isconnected = false;
            e.printStackTrace();
        }

    }


}
