package com.lansoeditor.demo;

import android.app.Application;
import android.os.Environment;

import com.lansosdk.videoeditor.LanSoEditor;
import com.lansosdk.videoeditor.LoadLanSongSdk;

/**
 * user：lqm
 * desc：
 */

public class App extends Application{
    public static final String mReceiveString = "ACTION_TEST";

    public static final String mPathString = Environment.getExternalStorageDirectory()+ "/" + "aa/";
    public static final String mPathStringB = Environment.getExternalStorageDirectory()+ "/" + "ab/";

    @Override
    public void onCreate() {
        super.onCreate();

        //加载so库,并初始化.
        LoadLanSongSdk.loadLibraries();
        LanSoEditor.initSo(getApplicationContext(),null);
    }
}
