package com.android.factory.knob;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.os.UserHandle;
import android.util.Log;
import android.view.KeyEvent;

import com.android.factory.R;
import com.android.factory.TTSBaseActivity;
import com.android.factory.headset.HeadSetActivity;
/**
 * KNOB测试
 */
public class KnobActivity extends TTSBaseActivity {
    private static final String STEPLESS_KNOB_UP[]={
            "com.talkpod.channel.left",
            "com.comlins.channel.left",
            "android.intent.action.CHANNELUP.down",
            "com.dfl.channel.left"
    };
    private static final String STEPLESS_KNOB_DOWN[]={
            "com.talkpod.channel.right",
            "com.comlins.channel.right",
            "android.intent.action.CHANNELDOWN.down",
            "com.dfl.channel.right"
    };
    //软开机
    private static final String POWERKEY_ON[]={
            "com.talkpod.soft.power.on",
            "com.comlins.soft.power.on",
            "com.xwh.action.POWERKEY_ON"
    };
    //软关机
    private static final String POWERKEY_OFF[]={
            "com.talkpod.soft.power.off",
            "com.comlins.soft.power.off",
            "com.xwh.action.POWERKEY_OFF"
    };

    private boolean isKnobKeyDown;
    private boolean isKnobKeyUp;
    private boolean isPowerKeyDown;
    private boolean isPowerKeyUp;
    private boolean isAllTestSuccess;
    private Intent mBroadCastIntent;
    private int mKnobKeyDownResult;
    private int mKnobKeyUpResult;
    private int mPowerOnResult;
    private int mPowerOffResult;
    private long[] mHits = new long[3];

    @Override
    protected void initData() {
        String mPlayText = getResources().getString(R.string.start_knob);
        if (mSystemTTS != null){
            mSystemTTS.playText(mPlayText);
        }
        registerCustomReceiver();
        mBroadCastIntent = new Intent();
    }

    private void registerCustomReceiver() {
        IntentFilter mIntentFilter = new IntentFilter();
        for (String mKnobUpAction : STEPLESS_KNOB_UP) {
            mIntentFilter.addAction(mKnobUpAction);
        }
        for (String mKnobDownAction : STEPLESS_KNOB_DOWN) {
            mIntentFilter.addAction(mKnobDownAction);
        }
        for (String mPowerOnAction : POWERKEY_ON) {
            mIntentFilter.addAction(mPowerOnAction);
        }
        for (String mPowerOffAction : POWERKEY_OFF) {
            mIntentFilter.addAction(mPowerOffAction);
        }
        registerReceiver(mKnobBroadCastReceiver,mIntentFilter);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_knob;
    }

    @Override
    public void handleMsg(Message msg) {
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mKnobBroadCastReceiver != null){
            unregisterReceiver(mKnobBroadCastReceiver);
            mKnobBroadCastReceiver = null;
        }
    }

    @Override
    protected void systemTTSComplete() {
        super.systemTTSComplete();
        if (mGlobalHandler != null && !isAllTestSuccess){
            mGlobalHandler.removeCallbacks(startKnobCodeComplete);
            mGlobalHandler.postDelayed(startKnobCodeComplete, 10*1000);
        }
        if (isAllTestSuccess){
            isTTSComplete = true;
        }
    }

    private final Runnable startKnobCodeComplete = new Runnable() {
        @Override
        public void run() {
            if (mSystemTTS != null){
                mSystemTTS.playText(getResources().getString(R.string.start_knob_fail));
                resetAllKnobTest();
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        mGlobalHandler.removeCallbacks(startKnobCodeComplete);
        mGlobalHandler.removeCallbacks(mKnobTestResult);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("knob_log","action onKeyDown------------>"+keyCode);
        switch (keyCode){
            case KeyEvent.KEYCODE_EXTERNAL_CHANNEL_DOWN:
                sendBroadCastKnobDown();
                isKnobKeyDown = true;
                return true;
            case KeyEvent.KEYCODE_EXTERNAL_CHANNEL_UP:
                sendBroadCastKnobUp();
                isKnobKeyUp = true;
                return true;
            case KeyEvent.KEYCODE_CODER_POWER_LEFT:
                sendBroadCastPowerOn();
                isPowerKeyDown = true;
                return true;
            case KeyEvent.KEYCODE_CODER_POWER_RIGHT:
                sendBroadCastPowerOff();
                isPowerKeyUp = true;
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void startActivityIntentClass() {
        startActivityIntent(this, HeadSetActivity.class);
    }

    private BroadcastReceiver mKnobBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("knob_log","action------------>"+intent.getAction());
            String mCurrentAction = intent.getAction();
            if (mCurrentAction != null && !"".equals(mCurrentAction)){
                compareCurrentAction(mCurrentAction);
            }
            Log.d("knob_log","isKnobTestComplete------------>"+isKnobTestComplete());
            if (isKnobTestComplete()){
                knobTestTTSPlay();
            }
        }
    };

    private boolean isKnobTestComplete() {
        return isKeyDownComplete() && isReceiverComplete();
    }


    private void compareCurrentAction(String mCurrentAction) {
        for (String s : STEPLESS_KNOB_UP) {
            if (mCurrentAction.equals(s)) {
                mKnobKeyDownResult = 1;
            }
        }
        for (String s : STEPLESS_KNOB_DOWN) {
            if (mCurrentAction.equals(s)) {
                mKnobKeyUpResult = 1;
            }
        }
        for (String s : POWERKEY_ON) {
            if (mCurrentAction.equals(s)) {
                mPowerOnResult = 1;
            }
        }
        for (String s : POWERKEY_OFF) {
            if (mCurrentAction.equals(s)) {
                mPowerOffResult = 1;
            }
        }
    }

    private void knobTestTTSPlay() {
        if (mGlobalHandler != null){
            mGlobalHandler.removeCallbacks(mKnobTestResult);
            mGlobalHandler.postDelayed(mKnobTestResult,2000);
        }
    }

    private final Runnable mKnobTestResult = new Runnable() {
        @Override
        public void run() {
            mGlobalHandler.removeCallbacks(startKnobCodeComplete);
            isAllTestSuccess = true;
            mSystemTTS.playText(getResources().getString(R.string.start_knob_success));
        }
    };

    public void sendBroadCastKnobDown(){
        for (String s : STEPLESS_KNOB_DOWN) {
            mBroadCastIntent.setAction(s);
            mContext.sendStickyBroadcastAsUser(mBroadCastIntent, UserHandle.ALL);
        }
		playAudio(this);
    }
    public void sendBroadCastKnobUp(){
        for (String s : STEPLESS_KNOB_UP) {
            mBroadCastIntent.setAction(s);
            mContext.sendStickyBroadcastAsUser(mBroadCastIntent, UserHandle.ALL);
        }
		playAudio(this);
    }
    public void sendBroadCastPowerOn(){
        for (String s : POWERKEY_ON) {
            mBroadCastIntent.setAction(s);
            mContext.sendStickyBroadcastAsUser(mBroadCastIntent, UserHandle.ALL);
        }
		playAudio(this);
    }
    public void sendBroadCastPowerOff(){
        for (String s : POWERKEY_OFF) {
            mBroadCastIntent.setAction(s);
            mContext.sendStickyBroadcastAsUser(mBroadCastIntent, UserHandle.ALL);
        }
		playAudio(this);
    }
    public boolean isKeyDownComplete(){
        return isKnobKeyDown && isKnobKeyUp && isPowerKeyDown && isPowerKeyUp;
    }

    public boolean isReceiverComplete(){
        return mKnobKeyDownResult == 1 && mKnobKeyUpResult ==1  && mPowerOnResult == 1 && mPowerOffResult == 1;
    }

    private void resetAllKnobTest() {
        isKnobKeyDown = false;
        isKnobKeyUp = false;
        isPowerKeyDown = false;
        isPowerKeyUp = false;
        mKnobKeyDownResult = 0;
        mKnobKeyUpResult = 0;
        mPowerOnResult = 0;
        mPowerOffResult = 0;
    }
}
