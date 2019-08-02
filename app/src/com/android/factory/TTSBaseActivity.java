package com.android.factory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.SystemTTS;
import android.view.KeyEvent;
import android.view.WindowManager;
import com.android.factory.handler.GlobalHandler;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.IAudioService;
import android.os.ServiceManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import android.util.Log;

public abstract class TTSBaseActivity extends Activity implements GlobalHandler.HandleMsgListener ,SystemTTS.ISpeechComplete,SoundPool.OnLoadCompleteListener{
    protected GlobalHandler mGlobalHandler;
    protected Context mContext;
    protected SystemTTS mSystemTTS;
    protected boolean isTTSComplete;
    private boolean isLongPress;

	protected SoundPool mSoundPool;
    protected static final int DEFAULT_INVALID_SOUND_ID = -1;
    protected int mSoundId = -1;
    protected int mStreamId = -1;
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
		if(mSoundPool == null){
		   mSoundPool = createSoundPool();
		}
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
        if (event.getKeyCode() == KeyEvent.KEYCODE_EXTERNAL_1) { //无屏测试 有屏KEYCODE_BACK
            event.startTracking();
			if(event.getRepeatCount() == 0){
		        isLongPress = false;
			}
        }
        return true;
    }
    protected abstract void startActivityIntentClass();

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_EXTERNAL_1){
            if (isLongPress){
                startActivityIntent(mContext,MainActivity.class);
            }else {
                if (isTTSComplete && event.getRepeatCount() == 0){
                    startActivityIntentClass();
                }
            }
            isLongPress = false;
        }
        return true;
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_EXTERNAL_1){
            isLongPress = true;
			return true;
        }
		return false;
    }
	//增加按键播放音效 -----------------------------------------------------
	public void playAudio(Context context){
        if (mSoundPool == null)return;
        mSoundPool.setOnLoadCompleteListener(this);
        if (mSoundId == DEFAULT_INVALID_SOUND_ID){
            mSoundId = mSoundPool.load(context.getApplicationContext(), R.raw.Talitha,0);
        }else{
            //if (mStreamId == DEFAULT_INVALID_SOUND_ID)
            onLoadComplete(mSoundPool,0,0);
        }
    }
	
	public SoundPool createSoundPool() {
        AudioAttributes mAudioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        mSoundPool = new SoundPool.Builder()
                .setMaxStreams(16)
                .setAudioAttributes(mAudioAttributes)
                .build();
        return mSoundPool;
    }

    @Override
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        if (mSoundPool != null){
			mSoundPool.stop(mStreamId);
            mStreamId = mSoundPool.play(mSoundId, 1.0f, 1.0f, 16, 0, 1.0f);
        }
    }
    
    public void releaseSound() {
        if (mSoundPool != null) {
            mSoundPool.autoPause();
            mSoundPool.unload(mSoundId);
            mSoundId = DEFAULT_INVALID_SOUND_ID;
            mSoundPool.release();
            mSoundPool = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
		releaseSound();
    }
	//增加按键播放音效-----------------------------------------------------
}
