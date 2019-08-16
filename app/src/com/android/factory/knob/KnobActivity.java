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
    private static final String STEPLESS_KNOB_UP = "com.custom.channel.up";
    private static final String STEPLESS_KNOB_DOWN= "com.custom.channel.right";
    private static final String POWERKEY_ON ="com.custom.power.on";
    private static final String POWERKEY_OFF = "com.custom.power.off";

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
        mIntentFilter.addAction(STEPLESS_KNOB_UP);
        mIntentFilter.addAction(STEPLESS_KNOB_DOWN);
        mIntentFilter.addAction(POWERKEY_ON);
        mIntentFilter.addAction(POWERKEY_OFF);
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
        //if (isAllTestSuccess){//旋钮测试和按键测试无关则
        isTTSComplete = true;
        //}
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
        if (mCurrentAction.equals(STEPLESS_KNOB_UP)) {
            mKnobKeyDownResult = 1;
        }
        if (mCurrentAction.equals(STEPLESS_KNOB_DOWN)) {
            mKnobKeyUpResult = 1;
        }
        if (mCurrentAction.equals(POWERKEY_ON)) {
            mPowerOnResult = 1;
        }
        if (mCurrentAction.equals(POWERKEY_OFF)) {
            mPowerOffResult = 1;
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
        mBroadCastIntent.setAction(STEPLESS_KNOB_DOWN);
        mContext.sendBroadcastAsUser(mBroadCastIntent, UserHandle.ALL);
		playAudio(this);
    }
    public void sendBroadCastKnobUp(){
        mBroadCastIntent.setAction(STEPLESS_KNOB_UP);
        mContext.sendBroadcastAsUser(mBroadCastIntent, UserHandle.ALL);
		playAudio(this);
    }
    public void sendBroadCastPowerOn(){
        mBroadCastIntent.setAction(POWERKEY_ON);
        mContext.sendBroadcastAsUser(mBroadCastIntent, UserHandle.ALL);
		playAudio(this);
    }
    public void sendBroadCastPowerOff(){
        mBroadCastIntent.setAction(POWERKEY_OFF);
        mContext.sendBroadcastAsUser(mBroadCastIntent, UserHandle.ALL);
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
