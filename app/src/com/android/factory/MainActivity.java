package com.android.factory;

import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

import com.android.factory.android.SystemExtraActivity;
import com.android.factory.device.DeviceVersionActivity;
import com.android.factory.knob.KnobActivity;

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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && isTTSComplete) { //有屏暂时代替测试
            startActivityIntent(this, SystemExtraActivity.class);
        }
        return true;
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
