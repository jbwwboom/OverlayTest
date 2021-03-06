package com.example.justi.overlaytest;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import java.util.Locale;
import java.util.Random;

public class OverlayVoiceService extends Service implements OnTouchListener, OnClickListener, OverlayListener, OnInitListener {

    private View topLeftView;

    private Button overlayedButton;
    private float offsetX;
    private float offsetY;
    private int originalXPos;
    private int originalYPos;
    private boolean moving;
    private WindowManager wm;

    //For timer
    private int i = 0;

    //For TTS
    private TextToSpeech tts;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        tts = new TextToSpeech(getApplicationContext(), this);

        //For timer
        OverlayChanger.getInstance(this).handlerEvent();

        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        overlayedButton = new Button(this);
        //tts
        String speed = "Snelheid: " + i + " km/h";
        tts.speak(speed,TextToSpeech.QUEUE_FLUSH,null,null);
        overlayedButton.setText("Snelheid: " + i + " km/h");


        overlayedButton.setOnTouchListener(this);
        //overlayedButton.setAlpha(0.0f);
        //overlayedButton.setBackgroundColor(0x55fe4444);
        overlayedButton.setBackgroundColor(Color.argb(0,0,0,0));
        //overlayedButton.setBackgroundColor(Color.WHITE);
        overlayedButton.setOnClickListener(this);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, LAYOUT_FLAG, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = 0;
        params.y = 0;
        wm.addView(overlayedButton, params);

        /*for(int i = 0; i <= 120; i += 60){
            ImageView chatHead = new ImageView(this);
            chatHead.setImageResource(R.drawable.red_circle);

            WindowManager.LayoutParams paramsCircle = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    LAYOUT_FLAG,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            paramsCircle.gravity = Gravity.TOP | Gravity.LEFT;
            paramsCircle.x = i * 4;
            paramsCircle.y = 0;

            wm.addView(chatHead, paramsCircle);
        }*/

        topLeftView = new View(this);
        WindowManager.LayoutParams topLeftParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, LAYOUT_FLAG, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        topLeftParams.gravity = Gravity.LEFT | Gravity.TOP;
        topLeftParams.x = 0;
        topLeftParams.y = 0;
        topLeftParams.width = 0;
        topLeftParams.height = 0;
        wm.addView(topLeftView, topLeftParams);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayedButton != null) {
            wm.removeView(overlayedButton);
            wm.removeView(topLeftView);
            overlayedButton = null;
            topLeftView = null;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getRawX();
            float y = event.getRawY();

            moving = false;

            int[] location = new int[2];
            overlayedButton.getLocationOnScreen(location);

            originalXPos = location[0];
            originalYPos = location[1];

            offsetX = originalXPos - x;
            offsetY = originalYPos - y;

        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            int[] topLeftLocationOnScreen = new int[2];
            topLeftView.getLocationOnScreen(topLeftLocationOnScreen);

            System.out.println("topLeftY="+topLeftLocationOnScreen[1]);
            System.out.println("originalY="+originalYPos);

            float x = event.getRawX();
            float y = event.getRawY();

            WindowManager.LayoutParams params = (LayoutParams) overlayedButton.getLayoutParams();

            int newX = (int) (offsetX + x);
            int newY = (int) (offsetY + y);

            if (Math.abs(newX - originalXPos) < 1 && Math.abs(newY - originalYPos) < 1 && !moving) {
                return false;
            }

            params.x = newX - (topLeftLocationOnScreen[0]);
            params.y = newY - (topLeftLocationOnScreen[1]);

            wm.updateViewLayout(overlayedButton, params);
            moving = true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (moving) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(this, "Overlay button click event", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void timerEvent() {
        i = new Random().nextInt(100);
        String speed = "Snelheid: " + i + " km/h";
        overlayedButton.setText(speed);
        OverlayChanger.getInstance(this).handlerEvent();

        if (!tts.isSpeaking()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts.speak(speed,TextToSpeech.QUEUE_FLUSH,null,null);
            } else {
                tts.speak(speed, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    @Override
    public void onInit(int status) {
        if (status  ==  TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.getDefault());
        } else {
            tts = null;
            Toast.makeText(this, "Failed to initialize TTS engine.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
