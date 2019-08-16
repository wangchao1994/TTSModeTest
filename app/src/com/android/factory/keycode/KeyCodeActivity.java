package com.android.factory.keycode;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import com.android.factory.R;
import com.android.factory.TTSBaseActivity;
import com.android.factory.knob.KnobActivity;

public class KeyCodeActivity extends TTSBaseActivity {
    private boolean key_external_1_tested = false;
    private boolean key_external_ptt_tx_tested = false;
    private boolean key_external_2_tested = false;
    private boolean key_external_sos_tested = false;
    private boolean isKeyCodeSuccess;
    private long[] mHits = new long[2];

    @Override
    protected void initData() {
        String mPlayText = getResources().getString(R.string.start_keycode);
        if (mSystemTTS != null){
            mSystemTTS.playText(mPlayText);
        }
    }

    private final Runnable startKeyCodeComplete = new Runnable() {
        @Override
        public void run() {
            if (mSystemTTS != null){
                mSystemTTS.playText(getResources().getString(R.string.start_key_fail));
                resetAllKeyTestValues();
            }
        }
    };

    private void resetAllKeyTestValues() {
        key_external_1_tested = false;
        key_external_ptt_tx_tested = false;
        key_external_2_tested = false;
        key_external_sos_tested = false;
    }

    @Override
    public void systemTTSComplete() {
        if (mGlobalHandler != null && !isKeyCodeSuccess){
            mGlobalHandler.removeCallbacks(startKeyCodeComplete);
            mGlobalHandler.postDelayed(startKeyCodeComplete, 10*1000);
        }
        if (isKeyCodeSuccess){
            isTTSComplete = true;
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_keycode;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGlobalHandler.removeCallbacks(startKeyCodeComplete);
        mGlobalHandler.removeCallbacks(mKeyTestResult);
    }

    @Override
    public void handleMsg(Message msg) {
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("knob_log","onKeyDown------------>"+ keyCode);
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK://无屏测试
                if (!isTTSComplete){//没有测试成功
                    voidStartNextItem();
                }/*else if(event.getRepeatCount() == 0){
                    startActivityIntentClass();
                }*/
                return true;
            case KeyEvent.KEYCODE_EXTERNAL_PTT_TX:
                Log.d("knob_log","onKeyDown---PTT_TX--------->"+ keyCode);
                key_external_ptt_tx_tested = true;
                voidStartIntentNextTestItem();
                return true;
            case KeyEvent.KEYCODE_EXTERNAL_2:
                key_external_2_tested = true;
                voidStartIntentNextTestItem();
                return true;
            case KeyEvent.KEYCODE_EXTERNAL_SOS:
                key_external_sos_tested = true;
                voidStartIntentNextTestItem();
                return true;
        }
        return super.onKeyDown(keyCode,event);
    }
    
    //解决重复按键测试失败后无法进行下一项测试
    private void voidStartNextItem() {
        System.arraycopy(mHits, 1, mHits, 0, mHits.length-1);
        mHits[mHits.length-1] = SystemClock.uptimeMillis();
        if ((SystemClock.uptimeMillis()-mHits[0]) <= 1500) {
            startPlaySystemRing(mContext);
            startActivityIntentClass();
        }else{
            key_external_1_tested = true;
            voidStartIntentNextTestItem();
        }
    }

    @Override
    protected void startActivityIntentClass() {
        startActivityIntent(this, KnobActivity.class);
    }

    private void voidStartIntentNextTestItem() {
        startPlaySystemRing(mContext);
        if (mGlobalHandler != null){
            mGlobalHandler.removeCallbacks(mKeyTestResult);
            if (isKeyCodeComplete()){
                mGlobalHandler.postDelayed(mKeyTestResult,2000);
            }
        }
    }

    public boolean isKeyCodeComplete(){
        return key_external_1_tested && key_external_ptt_tx_tested && key_external_2_tested;
    }
    private final Runnable mKeyTestResult = new Runnable() {
        @Override
        public void run() {
            mGlobalHandler.removeCallbacks(startKeyCodeComplete);
            isKeyCodeSuccess = true;
            mSystemTTS.playText(getResources().getString(R.string.start_key_success));
        }
    };

    private void startPlaySystemRing(Context context){
		playAudio(context);
    }
}
