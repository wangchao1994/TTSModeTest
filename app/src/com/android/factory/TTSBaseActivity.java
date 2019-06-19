package com.android.factory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.SystemTTS;
import android.util.Log;
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



}
