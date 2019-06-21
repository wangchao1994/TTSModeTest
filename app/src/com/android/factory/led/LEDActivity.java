package com.android.factory.led;

import android.content.Context;
import android.graphics.Color;
import android.os.Message;
import com.android.factory.R;
import com.android.factory.TTSBaseActivity;
import com.android.factory.keycode.KeyCodeActivity;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Led测试
 */
public class LEDActivity extends TTSBaseActivity {
    private int mCurrentShowIndex;
    private Timer mTimer = new Timer();
    @Override
    protected void initData() {
        String mPlayText = getResources().getString(R.string.start_led);
        if (mSystemTTS != null){
            mSystemTTS.playText(mPlayText);
        }
        mTimer.scheduleAtFixedRate(new TimerTask() {
            public void run(){
                if (mGlobalHandler != null){
                    mGlobalHandler.post(startLEDFactoryMode);
                }
            }
        }, 0, 500);
    }
    @Override
    protected int getLayoutId() {
        return R.layout.activity_led;
    }

    @Override
    public void handleMsg(Message msg) {
    }

    @Override
    protected void onPauseTasks() {
        super.onPauseTasks();
        if (mTimer != null){
            mTimer.cancel();
        }
        setCurrentLEDVales(3,mContext);//close led
        mGlobalHandler.removeCallbacks(startLEDFactoryMode);
    }

    private final Runnable startLEDFactoryMode = new Runnable() {
        @Override
        public void run() {
            if (mCurrentShowIndex >2)mCurrentShowIndex = 0;
            setCurrentLEDVales(mCurrentShowIndex,mContext);
            mCurrentShowIndex +=1;
        }
    };

    public void setCurrentLEDVales(int value, Context context){
        switch(value){
            case 0:
                LEDUtil.noticeLED(context,1,500,0);
                break;
            case 1:
                LEDUtil.noticeLED(context, Color.BLUE,500,0);
                break;
            case 2:
                LEDUtil.noticeLED(context,2,500,0);
                break;
            case 3:
                LEDUtil.noticeLED(context,-1,500,0);
                break;
        }
    }

    @Override
    protected void systemTTSComplete() {
        super.systemTTSComplete();
        isTTSComplete = true;
    }

    @Override
    protected void startActivityIntentClass() {
        startActivityIntent(this, KeyCodeActivity.class);
    }
}
