package com.lansoeditor.demo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.lansoeditor.demo.helper.MyVideoEditor;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private boolean isPermissionOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(this, new PermissionsResultAction() {
            @Override
            public void onGranted() {
                isPermissionOk=true;
                Toast.makeText(MainActivity.this, "权限已授予", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onDenied(String permission) {
                isPermissionOk=false;
                String message = String.format(Locale.getDefault(), "没有相关的权限", permission);
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

    }


    public void onRecoder(View view){
        startActivity(new Intent(MainActivity.this,VideoRecordActivity.class));
    }

    public void onCup(View view){
        startActivity(new Intent(MainActivity.this,VideoCupActivity.class));
    }
    public void onMixVideo(View view){
        Intent intent = new Intent(MainActivity.this,VideoRecordActivity.class);
        intent.putExtra("isMixVideo",true);
        startActivity(intent);
    }

    public void picPlayPost(View view){
        startActivity(new Intent(MainActivity.this,PPPActivity.class));
    }
    public void cupMp3(View view){
        MyVideoEditor editor = new MyVideoEditor();
        String srcAudio = Environment.getExternalStorageDirectory()+"/ab/music.mp3";
        String srcAudio2 = Environment.getExternalStorageDirectory()+"/ab/musicout.mp3";
        editor.executeAudioCutOut(srcAudio, srcAudio2,0,10);

    }

    public void jamyo(View view){
        startActivity(new Intent(MainActivity.this,CommunityActivity.class));
    }



}
