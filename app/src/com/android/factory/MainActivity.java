package com.android.factory;

import android.os.Message;
import android.util.Log;

import com.android.factory.android.SystemExtraActivity;
import com.android.factory.device.DeviceVersionActivity;
import com.android.factory.headset.HeadSetActivity;

public class MainActivity extends TTSBaseActivity{
    @Override
    protected void initData() {
        if (mGlobalHandler != null){
            mGlobalHandler.postDelayed(startTTSFactoryMode,500);
        }
    }
    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    private final Runnable startTTSFactoryMode = new Runnable() {
        @Override
        public void run() {
            if (mSystemTTS != null){
                mSystemTTS.playText(getResources().getString(R.string.start_test));
            }
        }
    };

    @Override
    protected void startActivityIntentClass() {
        startActivityIntent(this, SystemExtraActivity.class);
    }

    @Override
    public void systemTTSComplete() {
        Log.d("speech_log","MainActivity speechComplete----------------->");
        isTTSComplete = true;
    }

    @Override
    public void handleMsg(Message msg) {
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGlobalHandler.removeCallbacks(startTTSFactoryMode);
    }
}
