package com.android.factory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.SystemTTS;
import android.view.KeyEvent;
import android.view.WindowManager;
import com.android.factory.handler.GlobalHandler;

public abstract class TTSBaseActivity extends Activity implements GlobalHandler.HandleMsgListener ,SystemTTS.ISpeechComplete{
    protected GlobalHandler mGlobalHandler;
    protected Context mContext;
    protected SystemTTS mSystemTTS;
    protected boolean isTTSComplete;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        mContext = this;
        mSystemTTS = SystemTTS.getInstance(mContext);
        mSystemTTS.setSpeechComplete(this);
        mGlobalHandler = GlobalHandler.getInstance();
        mGlobalHandler.setHandleMsgListener(this);
        initData();
    }

    protected abstract void initData();
    protected abstract int getLayoutId();

    public void startActivityIntent(Context packageContext, Class<?> cls) {
        Intent intent = new Intent(packageContext, cls);
        packageContext.startActivity(intent);
    }

    @Override
    public void speechComplete() {
        systemTTSComplete();
    }

    @Override
    public void speechError() {
        systemTTSError();
    }

    protected void systemTTSComplete() {
    }
    private void systemTTSError() {
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_EXTERNAL_1 && isTTSComplete) { //无屏测试 有屏KEYCODE_BACK
            if (mGlobalHandler != null){
                mGlobalHandler.postDelayed(startRepeatFactoryMode,3000);
            }
            if (event.getRepeatCount() == 0){
                startActivityIntentClass();
            }
        }
        return true;
    }
    protected abstract void startActivityIntentClass();

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK){
            removeRepeatFactoryMode();
        }
        return super.onKeyUp(keyCode, event);
    }

    public void removeRepeatFactoryMode(){
        if (mGlobalHandler != null){
            mGlobalHandler.removeCallbacks(startRepeatFactoryMode);
        }
    }

    private final Runnable startRepeatFactoryMode = new Runnable() {
        @Override
        public void run() {
            startActivityIntent(mContext,MainActivity.class);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        removeRepeatFactoryMode();
    }


}
