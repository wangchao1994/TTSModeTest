package com.android.factory.imei;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;

import com.android.factory.R;
import com.android.factory.TTSBaseActivity;
import com.android.factory.led.LEDActivity;

import java.util.ArrayList;
import java.util.List;

public class IMEIActivity extends TTSBaseActivity {

    @Override
    protected void initData() {
        List<String> deviceIMEI = getDeviceIMEI();
        if (deviceIMEI != null && deviceIMEI.size() > 0){
            playText(deviceIMEI);
        }
    }

    @Override
    protected void systemTTSComplete() {
        super.systemTTSComplete();
        isTTSComplete = true;
    }
    @Override
    protected int getLayoutId() {
        return R.layout.activity_imei;
    }

    private void playText(List<String> deviceIMEI) {
        String mBuildPlayText = getResources().getString(R.string.start_imei);
        String mPlayText = String.format(mBuildPlayText, deviceIMEI.get(0));
        if (mSystemTTS != null){
            mSystemTTS.playText(mPlayText);
        }
    }

    private List<String> getDeviceIMEI() {
        List<String> mIMEIList = null;
        TelephonyManager telephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null){
            mIMEIList = new ArrayList<String>();
            for (int slotId = 0; slotId < telephonyManager.getPhoneCount(); slotId++) {
                String mIMEINumber = telephonyManager.getDeviceId(slotId);
                mIMEIList.add(mIMEINumber);
            }
        }
        return mIMEIList;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && isTTSComplete) { //有屏暂时代替测试
            startActivityIntent(this, LEDActivity.class);
        }
        return true;
    }
    @Override
    public void handleMsg(Message msg) {

    }
}
