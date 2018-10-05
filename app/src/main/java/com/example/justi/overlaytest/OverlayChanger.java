package com.example.justi.overlaytest;

import android.os.Handler;

public class OverlayChanger{

    private OverlayListener listener;
    private static OverlayChanger instance;


    private OverlayChanger(OverlayListener listener){
        this.listener = listener;
    }


    public static OverlayChanger getInstance(OverlayListener listener){
        if (instance == null){ //if there is no instance available... create new one
            instance = new OverlayChanger(listener);
        }

        return instance;
    }


    public void handlerEvent(){
        final Handler handler = new Handler();
        final Runnable r = new Runnable()
        {
            public void run()
            {
                listener.timerEvent();
            }
        };
        handler.postDelayed(r, 5000);
    }

}
