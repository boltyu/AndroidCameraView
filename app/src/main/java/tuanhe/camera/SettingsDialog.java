package tuanhe.camera;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.io.File;

public class SettingsDialog extends AlertDialog {

    private String filePath = "";
    private String fileName = "";
    private AlertDialog fileinfo_edit = null;

    protected SettingsDialog(final Context context) {
        super(context);
        final Builder fileinfo_builder = new Builder(context);
        LayoutInflater inflater = getLayoutInflater();
        final View mfilenamebox = inflater.inflate(R.layout.dialog_settings,null);
        final EditText mfilename = (EditText)mfilenamebox.findViewById(R.id.editTextPatientsName);
        fileinfo_builder.setTitle("患者信息");
        fileinfo_builder.setView(mfilenamebox)
            .setPositiveButton("继续", new DialogInterface.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.P)
                public void onClick(DialogInterface dialog, int id) {
                    //            // 创建根目录 "Cubie";
                File tmpdir = new File(filePath);
                tmpdir.mkdir();

                // 获取患者名称并且创建相应目录 "patients name"
                fileName = mfilename.getText().toString();  // Get patient's name
                filePath = Environment.getExternalStorageDirectory() + File.separator + "Cubie";
                if(fileName.length() < 1)
                    filePath = filePath + File.separator + "None";
                else
                    filePath = filePath + File.separator + mfilename.getText().toString();
                tmpdir = new File(filePath);
                tmpdir.mkdir();

                // 获取手术状态并且创建响应目录
                RadioButton tmpoption = (RadioButton) mfilenamebox.findViewById(R.id.beforeop);
                if(tmpoption.isChecked()) {
                    fileName = fileName + "_" + "术前";
                    filePath = filePath + File.separator + "术前";
                }else {
                    fileName = fileName + "_" + "术后";
                    filePath = filePath + File.separator + "术后";
                }
                tmpdir = new File(filePath);
                tmpdir.mkdir();

                tmpoption = (RadioButton) mfilenamebox.findViewById(R.id.usemedicine);
                if(tmpoption.isChecked()) {
                    fileName = fileName + "_" + "药后";
                    filePath = filePath + File.separator + "药后";
                }else {
                    fileName = fileName + "_" + "药前";
                    filePath = filePath + File.separator + "药前";
                }
                tmpdir = new File(filePath);
                tmpdir.mkdir();

                TextView mnote = (TextView)mfilenamebox.findViewById(R.id.editTextNotes);
                if(mnote.getText().length() > 0)
                    fileName = fileName + "_" + mnote.getText().toString();
                final Context pcontext = context;
                MainActivity.btnStart.post(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.btnStart.setVisibility(View.GONE);

                    }
                });
                hide();
                }
            });
        fileinfo_edit = fileinfo_builder.create();

    }

    protected SettingsDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    public void hide() {
        super.hide();
        fileinfo_edit.hide();
    }

    @Override
    public void show(){
        super.show();
        fileinfo_edit.show();
    }

    public String GetFileName(){
        if(fileName != "")
            return fileName;
        else
            return "None";
    }

    public String GetFilePath(){
        if(filePath != "")
            return filePath;
        else
            return "None";
    }

}
