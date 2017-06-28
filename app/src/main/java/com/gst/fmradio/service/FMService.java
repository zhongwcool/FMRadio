package com.gst.fmradio.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.gst.fmradio.R;
import com.gst.fmradio.bean.Contants;

/**
 * Created by yyshang on 7/8/15.
 */
public class FMService extends Service {
    int[] music = {R.raw.play1, R.raw.play2, R.raw.play3, R.raw.play4, R.raw.play5, R.raw.play6};
    int current = 0;
    private MediaPlayer mp;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (mp != null) {
            mp.stop();
            mp.release();
        }
        super.onDestroy();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.e("onStart","");
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int op = bundle.getInt("op");
                switch (op) {
                    case Contants.MEDIA_ORIGINAL:
                        if (current > 5) {
                            current = 0;
                        } else if (current < 0) {
                            current = 5;
                        }
                        Log.e("onStart","current"+current);
                        play();
                        current--;
                        break;
                    case Contants.MEDIA_PREVIOUS:
                        if (current > 5) {
                            current = 0;
                        } else if (current < 0) {
                            current = 5;
                        }
                        play();
                        current--;
                        break;
                    case Contants.MEDIA_NEXT:
                        if (current > 5) {
                            current = 0;
                        } else if (current < 0) {
                            current = 5;
                        }
                        play();
                        current++;
                        break;
                    case Contants.MEDIA_LATTER:
                        if (current > 5) {
                            current = 0;
                        } else if (current < 0) {
                            current = 5;
                        }
                        Log.e(" Service next:", "current " + current);
                        play();
                        current++;
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public void play() {
        if (mp == null) {
            mp = MediaPlayer.create(this, R.raw.play1);
            mp.setLooping(true);
        } else {
            mp.stop();
            mp.release();

            mp = MediaPlayer.create(this, music[current]);
            mp.setLooping(true);
        }
        mp.start();
    }


}
