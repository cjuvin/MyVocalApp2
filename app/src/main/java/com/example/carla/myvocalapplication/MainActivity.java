package com.example.carla.myvocalapplication;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaSync;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements VoiceManager.VoiceManagerListener {
    public final static int REQUEST_CODE_CHOOSE_ACTIVITY= 1;
    public final static int REQUEST_CODE_Shoplist= 2;
    public final static int REQUEST_CODE_CONTACT= 3;
    public final static int REQUEST_CODE_NO_LISTENING = 10;
    public  static int REQ_CODE = REQUEST_CODE_CHOOSE_ACTIVITY;


    VoiceManager voiceManager;


    private  String _strChooseAct = "Which activity do you want to start? ";
    private String _strRepete = "I didn't understand. Please repeat. ";
    private String _strList = "Shop List ";
    private String _strDir = "Directory";
    private TextView textView;
    private TextView shopButton;
    private TextView DirButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.txt);
        shopButton = (TextView)findViewById(R.id.shopTxt);
        DirButton = (TextView) findViewById(R.id.directoryTxt);
        voiceManager = new VoiceManager(this);
        voiceManager.setVoiceManagerListener(this);

        textView.setText(_strChooseAct);
        shopButton.setText(_strList);
        DirButton.setText(_strDir);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO}, 2);
        }
        else
        voiceManager.speak(REQUEST_CODE_CHOOSE_ACTIVITY, _strChooseAct);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode==2)
        {
            if (grantResults[0]== PackageManager.PERMISSION_GRANTED)
                voiceManager.setVoiceManagerListener(this);

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }



    @Override
    public void onResults(ArrayList<String> results, int type) {
        String _activity = VoiceManager.getString();

        if (_activity.contains("list"))
        {
            REQ_CODE = REQUEST_CODE_Shoplist;
            Intent otherAct = new Intent(getApplicationContext(),Shoplist.class);
            startActivity(otherAct);
        }

        else if (_activity.contains("irectory") && !(_activity.contains("list")))
        {
            REQ_CODE = REQUEST_CODE_CONTACT;
            Intent otherAct = new Intent(getApplicationContext(),Directory.class);
            startActivity(otherAct);
        }

        else
            voiceManager.speak(REQUEST_CODE_Shoplist, _strRepete);

    }

    @Override
    public void onSpeechCompleted(int requestCode) {

            voiceManager.listen(requestCode);
    }

}
