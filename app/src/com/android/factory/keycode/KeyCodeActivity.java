package com.android.factory.keycode;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Message;
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
            }
        }
    };

    @Override
    public void systemTTSComplete() {
        Log.d("speech_log","KeyCodeActivity speechComplete----------------->");
        if (mGlobalHandler != null && !isKeyCodeSuccess){
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
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK://有屏暂时代替测试
                if (!isTTSComplete){
                    key_external_1_tested = true;
                    voidStartIntentNextTestItem();
                }else if(event.getRepeatCount() == 0){
                    startActivityIntent(this, KnobActivity.class);
                }
                return true;
            case KeyEvent.KEYCODE_EXTERNAL_1://无屏测试
                return true;
            case KeyEvent.KEYCODE_EXTERNAL_PTT_TX:
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

    @Override
    protected void startActivityIntentClass() {
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
        return key_external_1_tested && key_external_ptt_tx_tested && key_external_2_tested /*&&key_external_sos_tested*/;
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
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone mRingtone = RingtoneManager.getRingtone(context.getApplicationContext(), uri);
        mRingtone.play();
    }
}
