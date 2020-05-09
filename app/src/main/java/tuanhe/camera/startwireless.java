package tuanhe.camera;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

import java.util.List;

class startwireless extends Thread {
    private boolean couldIstayhere = true;
    private String resultmessage = null;
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void run(){

        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                MainActivity.isconnected = false;
                if (success) {
                    ScanResult targetStation;
                    WifiConfiguration targetConfig = null;
                    List<ScanResult> results = MainActivity.wifiManager.getScanResults();
                    int r = 0;
                    for(r = 0; r < results.size(); r ++){
                        targetStation = results.get(r);
                        if(targetStation.SSID.equals("Cubie"))
                            break;
                    }
                    if(r < results.size()){
                        List<WifiConfiguration> wificonfig = MainActivity.wifiManager.getConfiguredNetworks();
                        int targetr = 0;
                        for (r = 0; r < wificonfig.size(); r++) {
                            if (wificonfig.get(r).SSID.equals("\"Cubie\"")) {
                                targetConfig = wificonfig.get(r);
                            } else {
                                MainActivity.wifiManager.disableNetwork(wificonfig.get(r).networkId);
                            }
                        }
                        if(targetConfig != null) {
                            MainActivity.wifiManager.disconnect();
                            MainActivity.wifiManager.addNetwork(targetConfig);
                            MainActivity.wifiManager.enableNetwork(targetConfig.networkId, true);
                            MainActivity.wifiManager.reconnect();
                            resultmessage = "触发模块已就绪";
                            MainActivity.isconnected = true;

                        }else{
                            resultmessage = "请手动连接模块";
                        }
                    }else{
                        resultmessage = "模块可能不在线";
                    }
                } else {
                    // scan failure handling
                    resultmessage = "在查找模块时遇到无线网络问题";
                }
                couldIstayhere = false;
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        MainActivity.global_context.registerReceiver(wifiScanReceiver, intentFilter);
        boolean success = MainActivity.wifiManager.startScan();
        if (!success) {
            // scan failure handling
            resultmessage = "无线网络无法使用";
        }else {
            while (couldIstayhere){
                if(MainActivity.wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED){
                    MainActivity.wifiManager.setWifiEnabled(true);
                }
            }
            MainActivity.global_context.unregisterReceiver(wifiScanReceiver);
        }

        MainActivity.textMessage.post(new Runnable() {
            @Override
            public void run() {
                MainActivity.textMessage.setText(MainActivity.sv.GetFileName() + "\n" + resultmessage);
            }
        });
    }
}
