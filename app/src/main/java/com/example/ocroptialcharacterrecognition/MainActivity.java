package com.example.ocroptialcharacterrecognition;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

import static android.Manifest.permission_group.CAMERA;

public class MainActivity extends AppCompatActivity {


    private TextView textView;
    private SurfaceView surfaceView;
    private CameraSource cameraSource;
    private TextRecognizer textRecognizer;
    private TextToSpeech textToSpeech;
    private String stringResult = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, PackageManager.PERMISSION_GRANTED);//


        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {

            }

        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraSource.release();
    }



    private void resultObtained(){

        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        textView.setText(stringResult);
        textToSpeech.speak(stringResult, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void textRecognizer() {

        textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer).setRequestedPreviewSize(1280, 1024).build();
        surfaceView = findViewById(R.id.surfaceView);

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                //eu vreau sa apelez cameraSource.start, dar am nevoie de try catch ul asta in caz ca se intampla una din cele doua exceptii:
                //daca atunci cand eu i am cerut permisiunea pt a ii folosi camera, userul a refuzat == SecurityException
                //iar IOException cred ca e daca are camera stricata, sau pur si simplu nu o gaseste, dar nu sunt 101% sigura

                try {

                    cameraSource.start(surfaceView.getHolder());

                }
                catch (IOException e) {

                    e.printStackTrace();

                }
                catch(SecurityException e){

                    e.printStackTrace();

                }


            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

                cameraSource.stop();

            }
        });


        textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(@NonNull Detector.Detections<TextBlock> detections) {

                SparseArray<TextBlock> sparseArray = detections.getDetectedItems();
                StringBuilder stringBuilder = new StringBuilder();

                for(int i = 0; i < sparseArray.size(); ++i) {

                    TextBlock textBlock = sparseArray.valueAt(i);

                    if(textBlock != null && textBlock.getValue() != null){

                        stringBuilder.append(textBlock.getValue() + " ");

                    }
                }

                final String stringText = stringBuilder.toString();

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable(){
                    @Override

                    public void run(){
                        stringResult = stringText;
                        resultObtained();
                    }
                });
            }
        });

    }









    public void startReadingButton(View view){ //asta e buttonStart din video

//        setContentView(R.layout.surfaceview);
//        textRecognizer();
    }
}
