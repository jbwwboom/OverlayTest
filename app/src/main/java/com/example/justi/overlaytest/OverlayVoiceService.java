package com.example.justi.overlaytest;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import java.util.Locale;
import java.util.Random;

public class OverlayVoiceService extends Service implements OnTouchListener, OnClickListener, OverlayListener, OnInitListener {

    private View topLeftView;

    private Button overlayedButton;
    private ImageButton closeOverlayButton;
    private ImageView chatHead;
    private float offsetX;
    private float offsetY;
    private int originalXPos;
    private int originalYPos;
    private boolean moving;
    private WindowManager wm;
    private OverlayChanger overlayChanger;


    //X-Y Params
    private int chatheadX = 70;
    private int chatheadY = 50;
    private int closeX = 35;
    private int closeY = 0;
    private int overlayX = 40;
    private int overlayY = 40;

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
        overlayChanger = new OverlayChanger(this);
        overlayChanger.handlerEvent();

        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        chatHead = new ImageView(this);
        chatHead.setImageResource(R.drawable.blue_rectangle);
        chatHead.setOnTouchListener(this);

        WindowManager.LayoutParams paramsRect = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        paramsRect.gravity = Gravity.TOP | Gravity.LEFT;
        paramsRect.x = chatheadX;
        paramsRect.y = chatheadY;

        wm.addView(chatHead, paramsRect);

        overlayedButton = new Button(this);
        //tts
        String speed = "Snelheid: " + i + " km/h";
        tts.speak(speed,TextToSpeech.QUEUE_FLUSH,null,null);
        //overlayedButton.setText(speed);
        overlayedButton.setText(i + "");


        overlayedButton.setOnTouchListener(this);
        overlayedButton.setBackgroundColor(Color.argb(0,0,0,0));
        overlayedButton.setTextColor(Color.WHITE);
        overlayedButton.setTextSize(30.0F);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, LAYOUT_FLAG, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = overlayX;
        params.y = overlayY;
        wm.addView(overlayedButton, params);

        closeOverlayButton = new ImageButton(this);
        closeOverlayButton.setImageResource(R.drawable.ic_close_red_24dp);

        closeOverlayButton.setBackgroundColor(Color.argb(0,0,0,0));
        closeOverlayButton.setOnClickListener(this);

        WindowManager.LayoutParams closeParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, LAYOUT_FLAG, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        closeParams.gravity = Gravity.LEFT | Gravity.TOP;
        closeParams.x = closeX;
        closeParams.y = closeY;
        wm.addView(closeOverlayButton, closeParams);

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
            wm.removeView(chatHead);
            wm.removeView(closeOverlayButton);
            overlayedButton = null;
            topLeftView = null;
            chatHead = null;
            closeOverlayButton = null;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getRawX();
            float y = event.getRawY();

            moving = false;

            int[] location = new int[2];
            chatHead.getLocationOnScreen(location);

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

            params.x = newX - (topLeftLocationOnScreen[0]) + overlayX;
            params.y = newY - (topLeftLocationOnScreen[1]) + overlayY;

            WindowManager.LayoutParams params2 = (LayoutParams) chatHead.getLayoutParams();
            params2.x = newX - (topLeftLocationOnScreen[0]) + chatheadX;
            params2.y = newY - (topLeftLocationOnScreen[1]) + chatheadY;

            WindowManager.LayoutParams params3 = (LayoutParams) closeOverlayButton.getLayoutParams();
            params3.x = newX - (topLeftLocationOnScreen[0]) + closeX;
            params3.y = newY - (topLeftLocationOnScreen[1]) + closeY;

            wm.updateViewLayout(overlayedButton, params);
            wm.updateViewLayout(chatHead, params2);
            wm.updateViewLayout(closeOverlayButton, params3);
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
        Toast.makeText(this, "Overlay uit", Toast.LENGTH_SHORT).show();
        this.onDestroy();
    }

    @Override
    public void timerEvent() {
        i = new Random().nextInt(100);
        String speed = i + "";
        if(overlayedButton != null){
            overlayedButton.setText(speed);
            overlayChanger.handlerEvent();
        }

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
